package com.nsw.im.service.friendship.controller;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.service.friendship.model.req.ApproverFriendRequestReq;
import com.nsw.im.service.friendship.model.req.GetFriendShipRequestReq;
import com.nsw.im.service.friendship.model.req.ReadFriendShipRequestReq;
import com.nsw.im.service.friendship.service.ImFriendShipRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 好友请求
 * @author nsw
 * @date 2023/8/15 22:18
 */
@RestController
@RequestMapping("v1/friendshipRequest")
public class ImFriendShipRequestController {


    @Autowired
    ImFriendShipRequestService imFriendShipRequestService;

    /**
     * 通过好友请求（审批）
     * @param req
     * @param appId
     * @param identifier
     * @return
     */
    @RequestMapping("/approveFriendRequest")
    public ResponseVO approveFriendRequest(@RequestBody @Validated
                                                   ApproverFriendRequestReq req, Integer appId, String identifier){
        req.setAppId(appId);
        req.setOperater(identifier);// 设置审批人
        return imFriendShipRequestService.approverFriendRequest(req);
    }

    @RequestMapping("/getFriendRequest")
    public ResponseVO getFriendRequest(@RequestBody @Validated GetFriendShipRequestReq req, Integer appId){
        req.setAppId(appId);
        return imFriendShipRequestService.getFriendRequest(req.getFromId(),req.getAppId());
    }

    /**
     * 好友请求已读 （设置为已读状态）
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/readFriendShipRequestReq")
    public ResponseVO readFriendShipRequestReq(@RequestBody @Validated ReadFriendShipRequestReq req, Integer appId){
        req.setAppId(appId);
        return imFriendShipRequestService.readFriendShipRequestReq(req);
    }


}