package com.nsw.im.service.group.service;

import com.nsw.im.codec.pack.message.ChatMessageAck;
import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.enums.command.GroupEventCommand;
import com.nsw.im.common.enums.command.MessageCommand;
import com.nsw.im.common.model.ClientInfo;
import com.nsw.im.common.model.message.GroupChatMessageContent;
import com.nsw.im.service.group.model.req.SendGroupMessageReq;
import com.nsw.im.service.message.model.resp.SendMessageResp;
import com.nsw.im.service.message.service.CheckSendMessageService;
import com.nsw.im.service.message.service.MessageStoreService;
import com.nsw.im.service.utils.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
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

    /**
     * 处理群消息
     * @param messageContent
     */
    public void process(GroupChatMessageContent messageContent) {

        String fromId = messageContent.getFromId();
        String toId = messageContent.getGroupId();
        Integer appId = messageContent.getAppId();
        // 前置校验
        // 这个用户是否被禁言，是否被禁用
        // 发送方和接受方是否是好友(非绝对的，有些app不是好友也可以发)
        ResponseVO responseVO = isServerPermissCheck(fromId, toId, appId);
        if (responseVO.isOk()) {
            // 存储群消息
            messageStoreService.storeGroupMessage(messageContent);
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



    private void ack(GroupChatMessageContent messageContent, ResponseVO responseVO) {
        log.info("msg ack, msgId={},checkResult:{}", messageContent.getMessageId(), responseVO.getCode());
        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);

        // 发消息 给发送方
        messageProducer.sendToUser(messageContent.getFromId(), GroupEventCommand.MSG_GROUP,
                responseVO, messageContent);
    }


    private void syncToSender(GroupChatMessageContent messageContent, ClientInfo clientInfo) {
        log.info("msg syncToSender, msgId={} ", messageContent.getMessageId());
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P, messageContent, clientInfo);
    }

    private void dispatchMessage(GroupChatMessageContent messageContent) {
        List<String> groupMemberId = imGroupMemberService.getGroupMemberId(messageContent.getGroupId(),
                messageContent.getAppId());
        for (String memberId : groupMemberId) {
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
