package com.nsw.im.service.friendship.controller;

import com.nsw.im.common.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nsw
 * @date 2023/8/15 22:16
 */
@RestController
@RequestMapping("v1/friendship/group")
public class ImFriendShipGroupController {


//    @Autowired
//    ImFriendShipGroupService imFriendShipGroupService;
//
//    @Autowired
//    ImFriendShipGroupMemberService imFriendShipGroupMemberService;
//
//
//    @RequestMapping("/add")
//    public ResponseVO add(@RequestBody @Validated AddFriendShipGroupReq req, Integer appId)  {
//        req.setAppId(appId);
//        return imFriendShipGroupService.addGroup(req);
//    }
//
//    @RequestMapping("/del")
//    public ResponseVO del(@RequestBody @Validated DeleteFriendShipGroupReq req, Integer appId)  {
//        req.setAppId(appId);
//        return imFriendShipGroupService.deleteGroup(req);
//    }
//
//    @RequestMapping("/member/add")
//    public ResponseVO (@RequestBody @Validated AddFriendShipGroupMemberReq req, Integer appId)  {
//        req.setAppId(appId);
//        return imFr//iendShipGroupMemberService.addGroupMember(req);
//    }
//    @RequestMapping("/member/del")
//    public ResponseVO memberdel(@RequestBody @Validated DeleteFriendShipGroupMemberReq req, Integer appId)  {
//        req.setAppId(appId);
//        return imFriendShipGroupMemberService.delGroupMember(req);
//    }


}