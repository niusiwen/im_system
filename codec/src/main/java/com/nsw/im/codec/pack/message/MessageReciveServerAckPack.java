package com.nsw.im.codec.pack.message;

import lombok.Data;

/**
 * 服务端发起的消息接收确认ack类（用户处于离线状态，由服务端发起消息接收确认）
 * @author nsw
 * @date 2024/1/25 16:44
 */
@Data
public class MessageReciveServerAckPack {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;

    /**
     * 是否是由服务端发起的
     */
    private Boolean serverSend;
}
