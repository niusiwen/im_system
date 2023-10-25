package com.nsw.im.service.user.controller;

import com.nsw.im.common.ClientType;
import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.route.RouteHandle;
import com.nsw.im.common.route.RouteInfo;
import com.nsw.im.common.utils.RouteInfoParseUtil;
import com.nsw.im.service.user.model.req.*;
import com.nsw.im.service.user.service.ImUserService;
import com.nsw.im.service.user.service.ImUserStatusService;
import com.nsw.im.service.utils.ZKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nsw
 * @date 2023/8/8 22:27
 */
@RestController
@RequestMapping("v1/user")
public class ImUserController {

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImUserStatusService imUserStatusService;

    @Autowired
    RouteHandle routeHandle;

    @Autowired
    ZKit zKit;

    /**
     * 批量导入用户数据
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.importUser(req);
    }

    /**
     * 批量删除用户
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.deleteUser(req);
    }

    /**
     * @param [req]
     * @return com.lld.im.common.ResponseVO
     * @description im的登录接口，返回im地址
     * @author chackylee
     */
    @RequestMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req, Integer appId) {
        req.setAppId(appId);

        ResponseVO login = imUserService.login(req);
        if (login.isOk()) {
            // todo 去zk获取一个im地址，返回给sdk
            List<String> allNode = new ArrayList<>();
            if (req.getClientType() == ClientType.WEB.getCode()) {
                // 获取所有节点地址
                allNode = zKit.getAllWebNode();
            } else {
                allNode = zKit.getAllTcpNode();
            }
            // 返回字符串 ip:port
            String s = routeHandle.routeServer(allNode, req
                    .getUserId());
            RouteInfo parse = RouteInfoParseUtil.parse(s);
            return ResponseVO.successResponse(parse);
        }

        return ResponseVO.errorResponse();
    }

    @RequestMapping("/getUserSequence")
    public ResponseVO getUserSequence(@RequestBody @Validated
                                              GetUserSequenceReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.getUserSequence(req);
    }

    @RequestMapping("/subscribeUserOnlineStatus")
    public ResponseVO subscribeUserOnlineStatus(@RequestBody @Validated
                                                        SubscribeUserOnlineStatusReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperater(identifier);
        imUserStatusService.subscribeUserOnlineStatus(req);
        return ResponseVO.successResponse();
    }

    @RequestMapping("/setUserCustomerStatus")
    public ResponseVO setUserCustomerStatus(@RequestBody @Validated
                                                    SetUserCustomerStatusReq req, Integer appId,String identifier) {
        req.setAppId(appId);
        req.setOperater(identifier);
        imUserStatusService.setUserCustomerStatus(req);
        return ResponseVO.successResponse();
    }

    @RequestMapping("/queryFriendOnlineStatus")
    public ResponseVO queryFriendOnlineStatus(@RequestBody @Validated
                                                      PullFriendOnlineStatusReq req, Integer appId,String identifier) {
        req.setAppId(appId);
        req.setOperater(identifier);
        return ResponseVO.successResponse(imUserStatusService.queryFriendOnlineStatus(req));
    }

    @RequestMapping("/queryUserOnlineStatus")
    public ResponseVO queryUserOnlineStatus(@RequestBody @Validated
                                                    PullUserOnlineStatusReq req, Integer appId,String identifier) {
        req.setAppId(appId);
        req.setOperater(identifier);
        return ResponseVO.successResponse(imUserStatusService.queryUserOnlineStatus(req));
    }

}
