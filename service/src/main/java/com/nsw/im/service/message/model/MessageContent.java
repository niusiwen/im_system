package com.nsw.im.service.message.model;

import com.nsw.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @author nsw
 * @date 2023/11/16 22:48
 */
@Data
public class MessageContent extends ClientInfo {

    private String messageId;

    private String fromId;

    private String toId;

    private String messageBody;
}
