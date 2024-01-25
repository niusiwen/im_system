package com.nsw.im.messagestore.model;

import com.nsw.im.common.model.message.MessageContent;
import com.nsw.im.messagestore.dao.ImMessageBodyEntity;
import lombok.Data;

/**
 * @description: 用于p2p消息持久化的DTO
 * @author: lld
 * @version: 1.0
 */
@Data
public class DoStoreP2PMessageDto {

    /**
     * 消息常量信息
     */
    private MessageContent messageContent;

    /**
     * 消息主题信息，用于消息存储到数据库
     */
    private ImMessageBodyEntity messageBody;

}
