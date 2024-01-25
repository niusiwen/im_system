package com.nsw.im.common.model.message;

import lombok.Data;
import sun.dc.pr.PRError;

/**
 * @description: 远程消息校验接口的请求参数
 * @author: lld
 * @version: 1.0
 */
@Data
public class CheckSendMessageReq {

    private String fromId;

    private String toId;

    private Integer appId;

    private Integer command;

}
