package com.nsw.im.common.model;

import lombok.Data;

/**
 * @author: niusiwen
 * @description: 客户端数据的拉取 入参类
 **/
@Data
public class SyncReq extends RequestBase {

    //客户端最大seq
    private Long lastSequence;
    //一次拉取多少
    private Integer maxLimit;

}
