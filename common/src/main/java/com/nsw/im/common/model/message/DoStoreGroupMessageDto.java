package com.nsw.im.common.model.message;

import lombok.Data;

/**
 * @description: 用于群消息持久化的dto
 * @author: lld
 * @version: 1.0
 */
@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBody messageBody;

}
