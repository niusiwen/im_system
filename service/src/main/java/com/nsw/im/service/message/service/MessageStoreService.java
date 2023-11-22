package com.nsw.im.service.message.service;

import com.nsw.im.common.enums.DelFlagEnum;
import com.nsw.im.common.model.message.GroupChatMessageContent;
import com.nsw.im.common.model.message.MessageContent;
import com.nsw.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.nsw.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.nsw.im.service.message.dao.ImMessageBodyEntity;
import com.nsw.im.service.message.dao.ImMessageHistoryEntity;
import com.nsw.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.nsw.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.nsw.im.service.utils.SnowflakeIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nsw
 * @date 2023/11/18 17:27
 */
@Service
public class MessageStoreService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

//    @Autowired
//    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;


    @Transactional
    public void storeP2PMessage(MessageContent messageContent) {
        // messageContent 转化成 messageBody
        ImMessageBodyEntity messageBodyEntity = extractMessageBody(messageContent);
        // 插入messageBody
        imMessageBodyMapper.insert(messageBodyEntity);
        // 转化成messageHistory
        List<ImMessageHistoryEntity> list = extractToP2PMessageHistory(messageContent, messageBodyEntity);
        // 批量插入
        imMessageHistoryMapper.insertBatchSomeColumn(list);

        //把messageKey 写回去
        messageContent.setMessageKey(messageBodyEntity.getMessageKey());
    }


    private ImMessageBodyEntity extractMessageBody(MessageContent messageContent) {
        ImMessageBodyEntity messageBody = new ImMessageBodyEntity();
        messageBody.setAppId(messageContent.getAppId());
        messageBody.setMessageKey(SnowflakeIdWorker.nextId());
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey("");
        messageBody.setExtra(messageContent.getExtra());
        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode());
        messageBody.setMessageTime(messageContent.getMessageTime());
        return messageBody;

    }

    private List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent,
                                                                   ImMessageBodyEntity messageBodyEntity){
        List<ImMessageHistoryEntity> list = new ArrayList<>();

        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(messageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(messageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }


    public void storeGroupMessage(GroupChatMessageContent messageContent) {
        // messageContent 转化成 messageBody
        ImMessageBodyEntity messageBodyEntity = extractMessageBody(messageContent);
        // 插入messageBody
        imMessageBodyMapper.insert(messageBodyEntity);
        // 转化成messageHistory
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, messageBodyEntity);
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);

        messageContent.setMessageKey(messageBodyEntity.getMessageKey());
    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent messageContent,
                                                                     ImMessageBodyEntity messageBodyEntity) {
        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, result);
        result.setGroupId(messageContent.getFromId());
        result.setMessageKey(messageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        return result;
    }


}
