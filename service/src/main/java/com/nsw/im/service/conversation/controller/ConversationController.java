package com.nsw.im.service.conversation.controller;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.service.conversation.model.DeleteConversationReq;
import com.nsw.im.service.conversation.model.UpdateConversationReq;
import com.nsw.im.service.conversation.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会话controller
 * @author nsw
 * @date 2024/2/4 17:21
 */
@RestController
@RequestMapping("/v1/conversation")
public class ConversationController {


    @Autowired
    ConversationService conversationService;

    /**
     * 删除会话
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/deleteConversation")
    public ResponseVO deleteConversation(@RequestBody @Validated DeleteConversationReq req, Integer appId) {
        req.setAppId(appId);
        return conversationService.deleteConversation(req);
    }

    /**
     * 更新会话
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/updateConversation")
    public ResponseVO updateConversation(@RequestBody @Validated UpdateConversationReq req, Integer appId) {
        req.setAppId(appId);
        return conversationService.updateConversation(req);
    }

}
