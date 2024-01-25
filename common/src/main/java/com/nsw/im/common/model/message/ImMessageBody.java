package com.nsw.im.common.model.message;

import lombok.Data;

/**
 * @description: 消息体信息，im_message_body表的实体类
 * @author: lld
 * @version: 1.0
 */
@Data
public class ImMessageBody {
    private Integer appId;

    /** messageBodyId*/
    private Long messageKey;

    /** messageBody*/
    private String messageBody;

    private String securityKey;

    private Long messageTime;

    private Long createTime;

    private String extra;

    private Integer delFlag;
}
