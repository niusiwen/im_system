package com.nsw.im.codec.proto;

import lombok.Data;

/**
 * @author nsw
 * @date 2023/9/28 22:29
 */
@Data
public class Message {

    private MessageHeader messageHeader;

    private Object messagePack;

    @Override
    public String toString() {
        return "Message{" +
                "messageHeader=" + messageHeader +
                ", messagePack=" + messagePack +
                '}';
    }
}
