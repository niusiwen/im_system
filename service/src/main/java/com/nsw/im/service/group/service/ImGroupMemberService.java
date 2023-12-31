package com.nsw.im.service.group.service;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.service.group.model.req.*;
import com.nsw.im.service.group.model.resp.GetRoleInGroupResp;

import java.util.Collection;
import java.util.List;

/**
 * @author nsw
 * @date 2023/9/22 22:17
 */
public interface ImGroupMemberService {

    public ResponseVO importGroupMember(ImportGroupMemberReq req);

    /**
     * 添加群成员
     * @param req
     * @return
     */
    public ResponseVO addMember(AddGroupMemberReq req);

    /**
     * 移除群成员
     * @param req
     * @return
     */
    public ResponseVO removeMember(RemoveGroupMemberReq req);

    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto);

    public ResponseVO removeGroupMember(String groupId, Integer appId, String memberId);

    public ResponseVO<GetRoleInGroupResp> getRoleInGroupOne(String groupId, String memberId, Integer appId);

    public ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req);

    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId, Integer appId);

    public List<String> getGroupMemberId(String groupId, Integer appId);

    public List<GroupMemberDto> getGroupManager(String groupId, Integer appId);

    /**
     * 修改群成员信息
     * @param req
     * @return
     */
    public ResponseVO updateGroupMember(UpdateGroupMemberReq req);

    /**
     * 转移群主
     * @param owner
     * @param groupId
     * @param appId
     * @return
     */
    public ResponseVO transferGroupMember(String owner, String groupId, Integer appId);

    /**
     * 禁言 群成员
     * @param req
     * @return
     */
    public ResponseVO speak(SpeaMemberReq req);

    ResponseVO<Collection<String>> syncMemberJoinedGroup(String operater, Integer appId);
}
