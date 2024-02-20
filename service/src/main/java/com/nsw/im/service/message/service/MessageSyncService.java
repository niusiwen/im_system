package com.nsw.im.service.message.service;

import com.nsw.im.codec.pack.message.MessageReadedPack;
import com.nsw.im.common.enums.command.Command;
import com.nsw.im.common.enums.command.GroupEventCommand;
import com.nsw.im.common.enums.command.MessageCommand;
import com.nsw.im.common.model.message.MessageReadedContent;
import com.nsw.im.common.model.message.MessageReceiveAckContent;
import com.nsw.im.service.conversation.service.ConversationService;
import com.nsw.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
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

    @Autowired
    ConversationService conversationService;


    /**
     * 服务端确认接收到消息发送给客户端的ack消息
     * @param messageReceiveAckContent
     */
    public void receiveMark(MessageReceiveAckContent messageReceiveAckContent) {

        messageProducer.sendToUser(messageReceiveAckContent.getToId(),
                MessageCommand.MSG_RECEIVE_ACK, messageReceiveAckContent, messageReceiveAckContent.getAppId());

    }


    /**
     * 消息已读：
     * 1、更新会话seq,
     * 2、通知在线的同步端，发送指定的command，
     * 3、发送已读回执通知对方（消息发起方）我已读
     * @param messageReadedContent
     */
    public void readMark(MessageReadedContent messageReadedContent) {
        // 1、更新会话seq,
        conversationService.messageMarkRead(messageReadedContent);

        // 2、通知在线的同步端，发送指定的command，
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReadedContent, messageReadedPack);
        syncToSender(messageReadedPack, messageReadedContent, MessageCommand.MSG_READED_NOTIFY);

        // 3、发送给对方 已读
        messageProducer.sendToUser(messageReadedContent.getToId(),
                MessageCommand.MSG_READED_RECEIPT, messageReadedPack, messageReadedContent.getAppId());


    }

    /**
     * 发送同步端已读通知
     * @param pack
     * @param content
     */
    private void syncToSender(MessageReadedPack pack, MessageReadedContent content, Command command) {
        // 发送给自己的其他端
        messageProducer.sendToUserExceptClient(pack.getFromId(),
                command, pack, content);

    }


    /**
     * 群聊消息已读
     * @param readedContent
     */
    public void groupReadMark(MessageReadedContent readedContent) {
        // 1、更新会话seq,
        conversationService.messageMarkRead(readedContent);

        // 2、通知在线的同步端，发送指定的command，
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(readedContent, messageReadedPack);
        syncToSender(messageReadedPack, readedContent, GroupEventCommand.MSG_GROUP_READED_NOTIFY);

        // 3、发送给对方 已读
        messageProducer.sendToUser(readedContent.getToId(),
                GroupEventCommand.MSG_GROUP_READED_RECEIPT, messageReadedPack, readedContent.getAppId());

    }

}
