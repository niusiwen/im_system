package com.nsw.im.service.group.service;

import com.nsw.im.codec.pack.message.ChatMessageAck;
import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.command.GroupEventCommand;
import com.nsw.im.common.enums.command.MessageCommand;
import com.nsw.im.common.model.ClientInfo;
import com.nsw.im.common.model.message.GroupChatMessageContent;
import com.nsw.im.common.model.message.OfflineMessageContent;
import com.nsw.im.service.group.model.req.SendGroupMessageReq;
import com.nsw.im.service.message.model.resp.SendMessageResp;
import com.nsw.im.service.message.service.CheckSendMessageService;
import com.nsw.im.service.message.service.MessageStoreService;
import com.nsw.im.service.seq.RedisSeq;
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
 * 群聊消息处理的service
 * @author nsw
 * @date 2023/11/18 16:23
 */
@Service
@Slf4j
public class GroupMessageService {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    MessageStoreService messageStoreService;

    @Autowired
    RedisSeq redisSeq;

    /**
     * 创建私有线程池 用于群聊服务端接收消息的处理
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
                thread.setName("groupMessage-process-thread-"+ num.getAndIncrement());
                return thread;
            }

        });
    }

    /**
     * 处理群消息
     * @param messageContent
     */
    public void process(GroupChatMessageContent messageContent) {

//        String fromId = messageContent.getFromId();
//        String toId = messageContent.getGroupId();
//        Integer appId = messageContent.getAppId();

        // 用messageId 从缓存中获取消息
        GroupChatMessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache(
                messageContent.getAppId(), messageContent.getMessageId(), GroupChatMessageContent.class);
        // 缓存中有，消息不需要持久化，只需要分发消息
        if (messageFromMessageIdCache != null) {
            threadPoolExecutor.execute(()->{
                // 1、 回ack成功给自己（客户端） 表示服务端已经收到了
                ack(messageContent, ResponseVO.successResponse());
                // 2、发消息给同步在线端
                syncToSender(messageContent, messageContent);
                // 3、发消息给所有群成員
                dispatchMessage(messageContent);
            });
            return;
        }

        // 群聊消息加上序列号
        long seq = redisSeq.deGetSeq(messageContent.getAppId() + ":" + Constants.SeqConstants.GroupMessage
                +":" + messageContent.getGroupId());
        messageContent.setMessageTime(seq);

        // 前置校验
        // 这个用户是否被禁言，是否被禁用
        // 发送方和接受方是否是好友(非绝对的，有些app不是好友也可以发)
//        ResponseVO responseVO = isServerPermissCheck(fromId, toId, appId);
//        if (responseVO.isOk()) {
        /**
         * 代码优化，消息处理使用线程池，消息持久化异步处理，消息合法化校验前置
         */
            threadPoolExecutor.execute(()->{
                // 存储群消息
                messageStoreService.storeGroupMessage(messageContent);

                List<String> groupMemberId = imGroupMemberService.getGroupMemberId(messageContent.getGroupId(),
                        messageContent.getAppId());
                messageContent.setMemberIds(groupMemberId);

                // 插入群离线消息 (使用redis的zset存儲)
                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                BeanUtils.copyProperties(messageContent, offlineMessageContent);
                offlineMessageContent.setToId(messageContent.getGroupId());
                messageStoreService.storeGroupOfflineMessage(offlineMessageContent, groupMemberId);


                // 1、 回ack成功给自己（客户端） 表示服务端已经收到了
                ack(messageContent, ResponseVO.successResponse());
                // 2、发消息给同步在线端
                syncToSender(messageContent, messageContent);
                // 3、发消息给所有群成员
                dispatchMessage(messageContent);

                //将messageId 存到缓存中
                messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(),
                        messageContent.getMessageId(), messageContent);
            });
//        } else {
//            // 不成功 告诉客户端失败了，也是ack
//            ack(messageContent, responseVO);
//        }


    }


    /**
     * 发送ack消息给发送方
     * @param messageContent
     * @param responseVO
     */
    private void ack(GroupChatMessageContent messageContent, ResponseVO responseVO) {
        log.info("msg ack, msgId={},checkResult:{}", messageContent.getMessageId(), responseVO.getCode());
        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);

        // 发消息 给发送方
        messageProducer.sendToUser(messageContent.getFromId(),
                GroupEventCommand.GROUP_MSG_ACK,
                responseVO, messageContent);
    }


    /**
     * 发送群消息给发送端的其他所有在线端
     * @param messageContent
     * @param clientInfo
     */
    private void syncToSender(GroupChatMessageContent messageContent, ClientInfo clientInfo) {
        log.info("msg syncToSender, msgId={} ", messageContent.getMessageId());
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                GroupEventCommand.MSG_GROUP, messageContent, clientInfo);
    }

    /**
     * 发送群消息给所有群成员
     * @param messageContent
     */
    private void dispatchMessage(GroupChatMessageContent messageContent) {

        for (String memberId : messageContent.getMemberIds()) {
            if (!memberId.equals(messageContent.getFromId() )) {
                messageProducer.sendToUser(memberId,
                        GroupEventCommand.MSG_GROUP,
                        messageContent, messageContent.getAppId());
            }
        }
    }

    private ResponseVO isServerPermissCheck(String fromId, String toId, Integer appId){
        ResponseVO responseVO = checkSendMessageService.checkGroupMessage(fromId, toId, appId);
        return responseVO;
    }

    /**
     * 发送群消息（接口调用）
     * @param req
     * @return
     */
    public SendMessageResp send(SendGroupMessageReq req) {
        SendMessageResp resp = new SendMessageResp();
        GroupChatMessageContent messageContent = new GroupChatMessageContent();
        BeanUtils.copyProperties(req, messageContent);

        // 存储群消息
        messageStoreService.storeGroupMessage(messageContent);
        resp.setMessageKey(messageContent.getMessageKey());
        resp.setMessageTime(System.currentTimeMillis());

        // 2、发消息给同步在线端
        syncToSender(messageContent, messageContent);
        // 3、发消息给对方在线端
        dispatchMessage(messageContent);
        return resp;
    }
}
