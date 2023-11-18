package com.nsw.im.service.message.service;

import com.nsw.im.codec.pack.message.ChatMessageAck;
import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.enums.command.MessageCommand;
import com.nsw.im.common.model.ClientInfo;
import com.nsw.im.service.message.model.MessageContent;
import com.nsw.im.service.utils.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
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

    /**
     *
     * @param messageContent
     */
    public void process(MessageContent messageContent) {

        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();
        // 前置校验
        // 这个用户是否被禁言，是否被禁用
        // 发送方和接受方是否是好友(非绝对的，有些app不是好友也可以发)
        ResponseVO responseVO = isServerPermissCheck(fromId, toId, messageContent);
        if (responseVO.isOk()) {
            // 1、 回ack成功给自己（客户端） 表示服务端已经收到了
            ack(messageContent, responseVO);
            // 2、发消息给同步在线端
            syncToSender(messageContent, messageContent);
            // 3、发消息给对方在线端
            dispatchMessage(messageContent);
        } else {
            // 不成功 告诉客户端失败了，也是ack
            ack(messageContent, responseVO);
        }


    }



    private void ack(MessageContent messageContent, ResponseVO responseVO) {
        log.info("msg ack, msgId={},checkResult:{}", messageContent.getMessageId(), responseVO.getCode());
        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);

        // 发消息 给发送方
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK,
                responseVO, messageContent);
    }


    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo) {
        log.info("msg syncToSender, msgId={} ", messageContent.getMessageId());
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P, messageContent, clientInfo);
    }

    private void dispatchMessage(MessageContent messageContent) {
        log.info("msg dispatchMessage, msgId={} ", messageContent.getMessageId());
        messageProducer.sendToUser(messageContent.getToId(),
                MessageCommand.MSG_P2P, messageContent, messageContent.getAppId());
    }

    private ResponseVO isServerPermissCheck(String fromId, String toId,
                                            MessageContent messageContent){

        ResponseVO responseVO = checkSendMessageService.checkSenderForbidAndMute(fromId, messageContent.getAppId());

        if (!responseVO.isOk()) {
            return responseVO;
        }

        responseVO = checkSendMessageService.checkFriendShip(fromId, toId, messageContent.getAppId());

        return responseVO;
    }

}
