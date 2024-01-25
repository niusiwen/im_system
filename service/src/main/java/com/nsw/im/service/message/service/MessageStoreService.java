package com.nsw.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.DelFlagEnum;
import com.nsw.im.common.model.message.DoStoreP2PMessageDto;
import com.nsw.im.common.model.message.GroupChatMessageContent;
import com.nsw.im.common.model.message.ImMessageBody;
import com.nsw.im.common.model.message.MessageContent;
import com.nsw.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.nsw.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.nsw.im.service.message.dao.ImMessageBodyEntity;
import com.nsw.im.service.message.dao.ImMessageHistoryEntity;
import com.nsw.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.nsw.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.nsw.im.service.utils.SnowflakeIdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Transactional
    public void storeP2PMessage(MessageContent messageContent) {
//        // messageContent 转化成 messageBody
//        ImMessageBodyEntity messageBodyEntity = extractMessageBody(messageContent);
//        // 插入messageBody
//        imMessageBodyMapper.insert(messageBodyEntity);
//        // 转化成messageHistory
//        List<ImMessageHistoryEntity> list = extractToP2PMessageHistory(messageContent, messageBodyEntity);
//        // 批量插入
//        imMessageHistoryMapper.insertBatchSomeColumn(list);
        //把messageKey 写回去
//        messageContent.setMessageKey(messageBodyEntity.getMessageKey());
        /**
         * 代码优化--> 异步消息持久化，将消息的持久化移到单独的服务，这里直接发送消息持久化的mq消息
         */
        ImMessageBody messageBody = extractMessageBody(messageContent);
        DoStoreP2PMessageDto dto = new DoStoreP2PMessageDto();
        dto.setMessageContent(messageContent);
        dto.setMessageBody(messageBody);
        //把messageKey 写回去
        messageContent.setMessageKey(messageBody.getMessageKey());
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreP2PMessage, "",
                JSONObject.toJSONString(dto));

    }


    /**
     * 参数转换为ImMessageBody实例类
     * messageContent --> ImMessageBody
     * @param messageContent
     * @return
     */
    private ImMessageBody extractMessageBody(MessageContent messageContent) {
        ImMessageBody messageBody = new ImMessageBody();
        messageBody.setAppId(messageContent.getAppId());
        messageBody.setMessageKey(SnowflakeIdWorker.nextId());
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey("");
        messageBody.setExtra(messageContent.getExtra());
        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode());
        messageBody.setMessageTime(messageContent.getMessageTime());
        return messageBody;

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


    public void storeGroupMessage(GroupChatMessageContent messageContent) {
//        // messageContent 转化成 messageBody
//        ImMessageBodyEntity messageBodyEntity = extractMessageBody(messageContent);
//        // 插入messageBody
//        imMessageBodyMapper.insert(messageBodyEntity);
//        // 转化成messageHistory
//        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, messageBodyEntity);
//        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
//
//        messageContent.setMessageKey(messageBodyEntity.getMessageKey());
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
