package com.nsw.im.common.model.message;

import com.nsw.im.common.model.ClientInfo;
import lombok.Data;

/**
 * 消息接收确认的实体类
 * @author nsw
 * @date 2023/12/2 16:51
 */
@Data
public class MessageReceiveAckContent extends ClientInfo {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;
}
