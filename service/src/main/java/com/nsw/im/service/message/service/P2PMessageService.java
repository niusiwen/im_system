package com.nsw.im.service.message.service;

import com.nsw.im.codec.pack.message.ChatMessageAck;
import com.nsw.im.codec.pack.message.MessageReciveServerAckPack;
import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.command.MessageCommand;
import com.nsw.im.common.model.ClientInfo;
import com.nsw.im.common.model.message.MessageContent;
import com.nsw.im.service.message.model.req.SendMessageReq;
import com.nsw.im.service.message.model.resp.SendMessageResp;
import com.nsw.im.service.seq.RedisSeq;
import com.nsw.im.service.utils.ConversationIdGenerate;
import com.nsw.im.service.utils.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单聊消息处理的service
 * @author nsw
 * @date 2023/11/16 22:54
 */
@Service
@Slf4j
public class P2PMessageService {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    MessageStoreService messageStoreService;

    @Autowired
    RedisSeq redisSeq;

    /**
     * 创建私有线程池 用于服务端接收消息的处理
     */
    private final ThreadPoolExecutor threadPoolExecutor;

    {
        AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true); //设置为守护线程
                thread.setName("message-process-thread-"+ num.getAndIncrement());
                return thread;
            }

        });
    }

    /**
     * 处理p2p消息
     * @param messageContent
     */
    public void process(MessageContent messageContent) {

//        String fromId = messageContent.getFromId();
//        String toId = messageContent.getToId();
//        Integer appId = messageContent.getAppId();

        // 先用messageId从缓存中获取消息，缓存中有，消息不需要持久化，只需要分发消息     -->用来保证消息的幂等性，防止数据库中重复存储消息
        MessageContent messageFromMessageIdCache = messageStoreService
                .getMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId(), MessageContent.class);
        if (messageFromMessageIdCache != null) {
            threadPoolExecutor.execute(()->{
                // 1、 回ack成功给自己（客户端） 表示服务端已经收到了
                ack(messageFromMessageIdCache, ResponseVO.successResponse());
                // 2、发消息给同步在线端
                syncToSender(messageFromMessageIdCache, messageFromMessageIdCache);
                // 3、发消息给对方在线端
                List<ClientInfo> clientInfos = dispatchMessage(messageFromMessageIdCache);
                if (clientInfos.isEmpty()) {
                    // 都为空，由服务端发送消息接收确认给发送方
                    revicerAck(messageFromMessageIdCache);
                }
            });
            return;
        }

        //单聊消息加上序列号： key = appId +":"+ seq +":"+ (from + to)/groupId  -->用来保证消息的有序性
        long seq = redisSeq.deGetSeq(messageContent.getAppId() + ":" +
                Constants.SeqConstants.Message + ":" +ConversationIdGenerate.generateP2PId(
                messageContent.getFromId(), messageContent.getToId()
        ));
        messageContent.setMessageSequence(seq);

        /**
         * 代码优化2->前置校验放在tcp层发送mq消息之前
         */
        // 前置校验
        // 这个用户是否被禁言，是否被禁用
        // 发送方和接受方是否是好友(非绝对的，有些app不是好友也可以发)
//        ResponseVO responseVO = isServerPermissCheck(fromId, toId, appId);
//        if (responseVO.isOk()) {
            /**
             * 代码优化1-->将服务端收到消息的处理处理逻辑放在线程池
             */
            threadPoolExecutor.execute(()->{
                // 插入数据到表里
                messageStoreService.storeP2PMessage(messageContent);
                // 1、 回ack成功给自己（客户端） 表示服务端已经收到了
                ack(messageContent, ResponseVO.successResponse());
                // 2、发消息给同步在线端
                syncToSender(messageContent, messageContent);
                // 3、发消息给对方在线端
                List<ClientInfo> clientInfos = dispatchMessage(messageContent);

                //将messageId 存到缓存中  --> 用来保证消息的幂等性，防止数据库中重复存储消息
                messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(),
                        messageContent.getMessageId(), messageContent);

                if (clientInfos.isEmpty()) {
                    // 都为空，由服务端发送消息接收确认给发送方
                    revicerAck(messageContent);
                }
            });


//        } else {
//            // 不成功 告诉客户端失败了，也是ack
//            ack(messageContent, responseVO);
//        }


    }


    /**
     * 服务端收到消息返回给发送方的ack方法
     * @param messageContent
     * @param responseVO
     */
    private void ack(MessageContent messageContent, ResponseVO responseVO) {
        log.info("msg ack, msgId={},checkResult:{}", messageContent.getMessageId(), responseVO.getCode());
        // 这里返回的ack消息也要加上序列号
        ChatMessageAck chatMessageAck = new
                ChatMessageAck(messageContent.getMessageId(), messageContent.getMessageSequence());
        responseVO.setData(chatMessageAck);

        // 发消息 给发送方
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK,
                responseVO, messageContent);
    }

    /**
     * 接收方离线，服务端发送消息接收确认的方法
     * @param messageContent
     */
    private void revicerAck(MessageContent messageContent) {
        MessageReciveServerAckPack pack = new MessageReciveServerAckPack();
        pack.setToId(messageContent.getFromId());
        pack.setFromId(messageContent.getToId());
        pack.setMessageKey(messageContent.getMessageKey());
        pack.setMessageSequence(messageContent.getMessageSequence());
        pack.setServerSend(true);
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_RECEIVE_ACK, pack,
                new ClientInfo(messageContent.getAppId(),
                        messageContent.getClientType(), messageContent.getImei()));
    }


    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo) {
        log.info("msg syncToSender, msgId={} ", messageContent.getMessageId());
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P, messageContent, clientInfo);
    }

    /**
     * 分发消息
     * @param messageContent
     */
    private List<ClientInfo> dispatchMessage(MessageContent messageContent) {
        log.info("msg dispatchMessage, msgId={} ", messageContent.getMessageId());
        List<ClientInfo> clientInfos = messageProducer.sendToUser(messageContent.getToId(),
                MessageCommand.MSG_P2P, messageContent, messageContent.getAppId());
        return clientInfos;
    }

    /**
     * 校验消息发送：1、用户是否被禁言，2、发送方与接收方是否是好友
     * @param fromId
     * @param toId
     * @param appId
     * @return
     */
    public ResponseVO isServerPermissCheck(String fromId, String toId,
                                            Integer appId){

        ResponseVO responseVO = checkSendMessageService.checkSenderForbidAndMute(fromId, appId);

        if (!responseVO.isOk()) {
            return responseVO;
        }

        responseVO = checkSendMessageService.checkFriendShip(fromId, toId, appId);

        return responseVO;
    }

    /**
     *  使用im服务的接入方后台（管理员）使用发送用户消息接口
     * @param req
     * @return
     */
    public SendMessageResp send(SendMessageReq req) {

        SendMessageResp resp = new SendMessageResp();
        MessageContent messageContent = new MessageContent();
        BeanUtils.copyProperties(req, messageContent);
        // 插入数据到表里
        messageStoreService.storeP2PMessage(messageContent);
        resp.setMessageKey(messageContent.getMessageKey());
        resp.setMessageTime(System.currentTimeMillis());

        // 2、发消息给同步在线端
        syncToSender(messageContent, messageContent);
        // 3、发消息给对方在线端
        dispatchMessage(messageContent);

        return resp;
    }

}
