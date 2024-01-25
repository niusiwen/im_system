package com.nsw.im.codec.pack.message;

import lombok.Data;

/**
 * 消息确认的ack类
 * @author nsw
 * @date 2023/11/18 15:02
 */
@Data
public class ChatMessageAck {

    private String messageId;

    public ChatMessageAck(String messageId) {
        this.messageId = messageId;
    }
}
