package com.nsw.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.nsw.im.common.ClientType;
import com.nsw.im.common.enums.command.Command;
import com.nsw.im.common.model.ClientInfo;
import com.nsw.im.service.group.service.ImGroupMemberService;
import com.nsw.im.service.group.service.ImGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 封装给群组用户发送消息的工具类
 * @author nsw
 * @date 2023/11/6 22:24
 */
@Component
public class GroupMessageProducer {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    /**
     * 给群组用户发送消息
     * @param userId 发起人userId
     * @param command
     * @param data
     * @param clientInfo 发起人信息
     */
    public void producer(String userId, Command command, Object data, ClientInfo clientInfo) {

        JSONObject o = (JSONObject) JSONObject.toJSON(data);

        String groupId = o.getString("groupId");
        // 获取群里所有群成员
        List<String> groupMemberId = imGroupMemberService
                .getGroupMemberId(groupId, clientInfo.getAppId());
        for (String memberId : groupMemberId) {
            // clientType等于webApi，说明是app发起的消息，并且当前群成员是发起人,即userId
            if (clientInfo.getClientType() != null && clientInfo.getClientType() ==
                    ClientType.WEBAPI.getCode() && memberId.equals(userId)) {
                // 发送给这个用户的其他端
                messageProducer.sendToUserExceptClient(memberId, command, data, clientInfo);
            }else {
                // 发给群组其他成员
                messageProducer.sendToUser(memberId, command, data, clientInfo.getClientType());
            }
        }

    }



}
