package com.nsw.im.common.model.message;

import lombok.Data;

/**
 * 群聊的消息体类
 * @author nsw
 * @date 2023/11/18 16:20
 */
@Data
public class GroupChatMessageContent extends MessageContent {

    private String groupId;
}
