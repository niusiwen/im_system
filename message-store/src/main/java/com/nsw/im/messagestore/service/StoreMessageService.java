package com.nsw.im.messagestore.service;

import com.nsw.im.common.model.message.MessageContent;
import com.nsw.im.messagestore.dao.ImMessageBodyEntity;
import com.nsw.im.messagestore.dao.ImMessageHistoryEntity;
import com.nsw.im.messagestore.dao.mapper.ImMessageBodyMapper;
import com.nsw.im.messagestore.dao.mapper.ImMessageHistoryMapper;
import com.nsw.im.messagestore.model.DoStoreP2PMessageDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     *
     * @param doStoreP2PMessageDto
     */
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

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(messageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }


}
