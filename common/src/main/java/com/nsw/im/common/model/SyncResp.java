package com.nsw.im.common.model;

import lombok.Data;

import java.util.List;

/**
 * @author: niusiwen
 * @description: 客户端数据的拉取 返回类
 **/
@Data
public class SyncResp<T> {

    private Long maxSequence;

    private boolean isCompleted;

    private List<T> dataList;

}
