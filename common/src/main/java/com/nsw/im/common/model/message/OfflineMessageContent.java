package com.nsw.im.common.model.message;

import lombok.Data;

/**
 * 离线消息
 * @author nsw
 * @date 2024/2/29 17:21
 */
@Data
public class OfflineMessageContent {

    private Integer appId;

    /** messageBodyId*/
    private Long messageKey;

    /** messageBody 消息体*/
    private String messageBody;

    private Long messageTime;

    /**拓展字段*/
    private String extra;

    /**删除标识*/
    private Integer delFlag;

    private String fromId;

    private String toId;

    /** 序列号*/
    private Long messageSequence;

    private String messageRandom;

    /** 会话类型*/
    private Integer conversationType;

    /** 会话id*/
    private String conversationId;

}
