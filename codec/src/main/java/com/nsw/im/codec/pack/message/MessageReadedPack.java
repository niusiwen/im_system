package com.nsw.im.codec.pack.message;

import lombok.Data;

/**
 * 消息已读的数据包（发送给tcp服务的数据包都要放在codec中）
 * @author nsw
 * @date 2024/2/5 14:21
 */
@Data
public class MessageReadedPack {

    private Long messageSeqence;

    private String fromId;

    private String groupId;

    private String toId;

    /**
     * 会话类型：单聊，群聊
     */
    private Integer conversationType;
}
