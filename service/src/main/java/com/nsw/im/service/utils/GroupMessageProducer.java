package com.nsw.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.nsw.im.codec.pack.group.AddGroupMemberPack;
import com.nsw.im.codec.pack.group.RemoveGroupMemberPack;
import com.nsw.im.codec.pack.group.UpdateGroupMemberPack;
import com.nsw.im.common.ClientType;
import com.nsw.im.common.enums.command.Command;
import com.nsw.im.common.enums.command.GroupEventCommand;
import com.nsw.im.common.model.ClientInfo;
import com.nsw.im.service.group.model.req.GroupMemberDto;
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

        if (command.equals(GroupEventCommand.ADDED_MEMBER)) {
            // 添加群成员发送给群主(管理员)和被添加者本身
            AddGroupMemberPack addGroupMemberPack = o.toJavaObject(AddGroupMemberPack.class);
            List<String> members = addGroupMemberPack.getMembers();
            List<GroupMemberDto> groupManager = imGroupMemberService.getGroupManager(groupId, clientInfo.getAppId());
            for (GroupMemberDto groupMemberDto : groupManager) {
                if(clientInfo.getClientType() != ClientType.WEBAPI.getCode() && groupMemberDto.getMemberId().equals(userId)){
                    messageProducer.sendToUserExceptClient(groupMemberDto.getMemberId(),command,data,clientInfo);
                }else{
                    messageProducer.sendToUser(groupMemberDto.getMemberId(),command,data,clientInfo.getAppId());
                }
            }
            for (String member : members) {
                if(clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.equals(userId)){
                    messageProducer.sendToUserExceptClient(member,command,data,clientInfo);
                }else{
                    messageProducer.sendToUser(member,command,data,clientInfo.getAppId());
                }
            }
        } else if (command.equals(GroupEventCommand.DELETED_MEMBER)) {
            // 移除群成员发送给所有群成员和被踢人
            RemoveGroupMemberPack removeGroupMemberPack = o.toJavaObject(RemoveGroupMemberPack.class);
            String member = removeGroupMemberPack.getMember();
            List<String> memberIds = imGroupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());
            memberIds.add(member);
            for (String memberId : memberIds) {
                if(clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.equals(userId)){
                    messageProducer.sendToUserExceptClient(memberId,command,data,clientInfo);
                }else{
                    messageProducer.sendToUser(memberId,command,data,clientInfo.getAppId());
                }
            }
        } else if (command.equals(GroupEventCommand.UPDATED_MEMBER)) {
            // 更新群成员信息，发送给群主(管理员)和被操作人
            UpdateGroupMemberPack updateGroupMemberPack = o.toJavaObject(UpdateGroupMemberPack.class);
            String memberId = updateGroupMemberPack.getMemberId();
            List<GroupMemberDto> groupManager = imGroupMemberService.getGroupManager(groupId, clientInfo.getAppId());
            GroupMemberDto groupMemberDto = new GroupMemberDto();
            groupMemberDto.setMemberId(memberId);
            groupManager.add(groupMemberDto);
            for (GroupMemberDto member : groupManager) {
                if(clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.equals(userId)){
                    messageProducer.sendToUserExceptClient(member.getMemberId(),command,data,clientInfo);
                }else{
                    messageProducer.sendToUser(member.getMemberId(),command,data,clientInfo.getAppId());
                }
            }
        } else {
            // 其他操作发送给全部群成员
            // 获取群里所有群成员的id
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



}
