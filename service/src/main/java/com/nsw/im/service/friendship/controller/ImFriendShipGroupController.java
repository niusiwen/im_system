package com.nsw.im.service.friendship.controller;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.nsw.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.nsw.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;
import com.nsw.im.service.friendship.model.req.DeleteFriendShipGroupReq;
import com.nsw.im.service.friendship.service.ImFriendShipGroupMemberService;
import com.nsw.im.service.friendship.service.ImFriendShipGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 群请求
 * @author nsw
 * @date 2023/8/15 22:16
 */
@RestController
@RequestMapping("v1/friendship/group")
public class ImFriendShipGroupController {


    @Autowired
    ImFriendShipGroupService imFriendShipGroupService;

    @Autowired
    ImFriendShipGroupMemberService imFriendShipGroupMemberService;


    @RequestMapping("/add")
    public ResponseVO add(@RequestBody @Validated AddFriendShipGroupReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendShipGroupService.addGroup(req);
    }

    @RequestMapping("/del")
    public ResponseVO del(@RequestBody @Validated DeleteFriendShipGroupReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendShipGroupService.deleteGroup(req);
    }

    @RequestMapping("/member/add")
    public ResponseVO memberadd(@RequestBody @Validated AddFriendShipGroupMemberReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendShipGroupMemberService.addGroupMember(req);
    }
    @RequestMapping("/member/del")
    public ResponseVO memberdel(@RequestBody @Validated DeleteFriendShipGroupMemberReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendShipGroupMemberService.delGroupMember(req);
    }


}