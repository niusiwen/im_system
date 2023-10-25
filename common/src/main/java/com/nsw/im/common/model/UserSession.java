package com.nsw.im.common.model;

import lombok.Data;

/**
 * @author nsw
 * @date 2023/9/30 16:09
 */
@Data
public class UserSession {

    private String userId;

    /**
     * 应用id
     */
    private Integer appId;

    /**
     * 端的标志
     */
    private Integer clientType;

    /**
     * sdk版本号
     */
    private Integer vsersion;

    /**
     * 连接状态 1在线 2离线
     */
    private Integer connectState;

    /**
     * 服务别名
     */
    private Integer brokerId;

    /**
     * 服务ip
     */
    private String  brokerHost;
}
