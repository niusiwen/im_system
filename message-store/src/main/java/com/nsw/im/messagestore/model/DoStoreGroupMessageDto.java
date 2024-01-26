package com.nsw.im.messagestore.model;

import com.nsw.im.common.model.message.GroupChatMessageContent;
import com.nsw.im.messagestore.dao.ImMessageBodyEntity;
import lombok.Data;

/**
 * @description: 用于群消息持久化的dto
 * @author: lld
 * @version: 1.0
 */
@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
