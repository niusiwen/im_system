package com.nsw.im.common.model;

import lombok.Data;

/**
 * @author nsw
 * @date 2023/9/30 17:31
 */
@Data
public class UserClientDto {

    private Integer appId;

    private Integer clientType;

    private String userId;

    private String imei;

}
