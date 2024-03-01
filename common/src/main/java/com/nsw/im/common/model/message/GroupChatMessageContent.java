package com.nsw.im.common.model.message;

import lombok.Data;

import java.util.List;

/**
 * 群聊的消息体类
 * @author nsw
 * @date 2023/11/18 16:20
 */
@Data
public class GroupChatMessageContent extends MessageContent {

    /**
     * 群id
     */
    private String groupId;

    /**
     * 群成员Id
     */
    private List<String> memberIds;
}
