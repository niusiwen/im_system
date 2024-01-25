package com.nsw.im.service.message.service;

import com.nsw.im.common.enums.command.MessageCommand;
import com.nsw.im.common.model.message.MessageReceiveAckContent;
import com.nsw.im.service.utils.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息同步
 * @author nsw
 * @date 2023/12/7 21:58
 */
@Service
public class MessageSyncService {

    @Autowired
    MessageProducer messageProducer;


    /**
     * 服务端确认接收到消息发送给客户端的ack消息
     * @param messageReceiveAckContent
     */
    public void receiveMark(MessageReceiveAckContent messageReceiveAckContent) {

        messageProducer.sendToUser(messageReceiveAckContent.getToId(),
                MessageCommand.MSG_RECEIVE_ACK, messageReceiveAckContent, messageReceiveAckContent.getAppId());

    }





}
