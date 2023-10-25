package com.nsw.im.service.friendship.service;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.service.friendship.model.req.ApproverFriendRequestReq;
import com.nsw.im.service.friendship.model.req.FriendDto;
import com.nsw.im.service.friendship.model.req.ReadFriendShipRequestReq;

/**
 * @author nsw
 * @date 2023/9/13 22:06
 */
public interface ImFriendShipRequestService {

    /**
     * 添加好友申请
     * @param fromId
     * @param dto
     * @param appId
     * @return
     */
    public ResponseVO addFienshipRequest(String fromId, FriendDto dto, Integer appId);

    /**
     * 审批好友申请
     * @param req
     * @return
     */
    public ResponseVO approverFriendRequest(ApproverFriendRequestReq req);

    /**
     * 读取好友申请的结果
     * @param req
     * @return
     */
    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req);

    /**
     * 获取好友申请
     * @param fromId
     * @param appId
     * @return
     */
    public ResponseVO getFriendRequest(String fromId, Integer appId);
}
