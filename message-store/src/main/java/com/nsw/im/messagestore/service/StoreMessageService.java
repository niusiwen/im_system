package com.nsw.im.messagestore.service;

import com.nsw.im.common.model.message.GroupChatMessageContent;
import com.nsw.im.common.model.message.MessageContent;
import com.nsw.im.messagestore.dao.ImGroupMessageHistoryEntity;
import com.nsw.im.messagestore.dao.ImMessageBodyEntity;
import com.nsw.im.messagestore.dao.ImMessageHistoryEntity;
import com.nsw.im.messagestore.dao.mapper.ImGroupMessageHistoryMapper;
import com.nsw.im.messagestore.dao.mapper.ImMessageBodyMapper;
import com.nsw.im.messagestore.dao.mapper.ImMessageHistoryMapper;
import com.nsw.im.messagestore.model.DoStoreGroupMessageDto;
import com.nsw.im.messagestore.model.DoStoreP2PMessageDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nsw
 * @date 2023/12/2 11:21
 */
@Service
public class StoreMessageService {

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    /**
     * 持久化单聊消息
     * @param doStoreP2PMessageDto
     */
    @Transactional
    public void doStoreP2PMessage(DoStoreP2PMessageDto doStoreP2PMessageDto) {

        imMessageBodyMapper.insert(doStoreP2PMessageDto.getMessageBody());

        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(doStoreP2PMessageDto.getMessageContent(), doStoreP2PMessageDto.getMessageBody());
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
    }

    /**
     * 参数转为 ImMessageHistoryEntity 实体类的List对象
     * @param messageContent
     * @param messageBodyEntity
     * @return
     */
    private List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent,
                                                                    ImMessageBodyEntity messageBodyEntity){
        List<ImMessageHistoryEntity> list = new ArrayList<>();

        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(messageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());
        // 持久化时加上序列号
        fromHistory.setSequence(messageContent.getMessageSequence());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(messageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());
        // 持久化时加上序列号
        toHistory.setSequence(messageContent.getMessageSequence());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }

    /**
     * 持久化群聊消息
     * @param doStoreGroupMessageDto
     */
    @Transactional
    public void doStoreGroupMessage(DoStoreGroupMessageDto doStoreGroupMessageDto) {
        imMessageBodyMapper.insert(doStoreGroupMessageDto.getImMessageBodyEntity());
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(doStoreGroupMessageDto.getGroupChatMessageContent(),doStoreGroupMessageDto.getImMessageBodyEntity());
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);

    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent
                                                                             messageContent , ImMessageBodyEntity messageBodyEntity){
        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        return result;
    }


}
