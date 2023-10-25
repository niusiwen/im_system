package com.nsw.im.service.user.controller;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.service.user.model.req.GetUserInfoReq;
import com.nsw.im.service.user.model.req.ModifyUserInfoReq;
import com.nsw.im.service.user.model.req.UserId;
import com.nsw.im.service.user.service.ImUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nsw
 * @date 2023/8/9 19:15
 */
@RestController
@RequestMapping("v1/user/data")
public class ImUserDataController {

    private static Logger logger = LoggerFactory.getLogger(ImUserDataController.class);

    @Autowired
    ImUserService imUserService;

    /**
     * 批量获取用户
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/getUserInfo")
    public ResponseVO getUserInfo(@RequestBody GetUserInfoReq req, Integer appId){//@Validated
        req.setAppId(appId);
        return imUserService.getUserInfo(req);
    }

    /**
     * 获取单个用户
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/getSingleUserInfo")
    public ResponseVO getSingleUserInfo(@RequestBody @Validated UserId req, Integer appId){
        req.setAppId(appId);
        return imUserService.getSingleUserInfo(req.getUserId(),req.getAppId());
    }

    /**
     * 更新用户信息
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/modifyUserInfo")
    public ResponseVO modifyUserInfo(@RequestBody @Validated ModifyUserInfoReq req, Integer appId){
        req.setAppId(appId);
        return imUserService.modifyUserInfo(req);
    }
}
