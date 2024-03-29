package com.nsw.im.service.message.controller;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.model.message.CheckSendMessageReq;
import com.nsw.im.service.message.model.req.SendMessageReq;
import com.nsw.im.service.message.service.P2PMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 给使用im服务的接入方后台（管理员）使用以及tcp服务远程调用的的接口
 * @author nsw
 * @date 2023/11/18 18:19
 */
@RestController
@RequestMapping("v1/message")
public class MessageController {

    @Autowired
    P2PMessageService p2PMessageService;

    /**
     * 使用im服务的接入方后台（管理员）使用发送用户消息接口
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/send")
    public ResponseVO send (@RequestBody @Validated SendMessageReq req,
                            Integer appId) {
        req.setAppId(appId);
        return ResponseVO.successResponse(p2PMessageService.send(req));

    }


    /**
     * 发送消息校验
     * @param req
     * @return
     */
    @RequestMapping("/checkSend")
    public ResponseVO checkSend (@RequestBody @Validated CheckSendMessageReq req) {
        return p2PMessageService.isServerPermissCheck(req.getFromId(), req.getToId(),
                req.getAppId());

    }

}
