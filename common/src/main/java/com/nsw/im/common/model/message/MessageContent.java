package com.nsw.im.common.model.message;

import com.nsw.im.common.model.ClientInfo;
import lombok.Data;

/**
 * 消息体类
 * @author nsw
 * @date 2023/11/16 22:48
 */
@Data
public class MessageContent extends ClientInfo {

    private String messageId;

    private String fromId;

    private String toId;

    private String messageBody;

    private Long  messageTime;

    private String extra;

    private Long messageKey;

    private long messageSequence;
}
