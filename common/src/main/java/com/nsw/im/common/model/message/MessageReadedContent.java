package com.nsw.im.common.model.message;

import com.nsw.im.common.model.ClientInfo;
import lombok.Data;

/**
 * 已读的消息体
 * @author nsw
 * @date 2024/2/4 17:37
 */
@Data
public class MessageReadedContent extends ClientInfo {

    private Long messageSeqence;

    private String fromId;

    private String groupId;

    private String toId;

    /**
     * 会话类型：单聊，群聊
     */
    private Integer conversationType;
}

