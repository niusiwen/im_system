package com.nsw.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nsw.im.common.ResponseVO;
import com.nsw.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.nsw.im.service.friendship.dao.ImFriendShipGroupMemberEntity;
import com.nsw.im.service.friendship.dao.mapper.ImFriendShipGroupMemberMapper;
import com.nsw.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.nsw.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;
import com.nsw.im.service.friendship.service.ImFriendShipGroupMemberService;
import com.nsw.im.service.friendship.service.ImFriendShipGroupService;
import com.nsw.im.service.user.dao.ImUserDataEntity;
import com.nsw.im.service.user.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nsw
 * @date 2023/9/14 22:44
 */
@Service
public class ImFriendShipGroupMemberServiceImpl implements ImFriendShipGroupMemberService {

    @Autowired
    ImFriendShipGroupMemberMapper imFriendShipGroupMemberMapper;

    @Autowired
    ImFriendShipGroupService imFriendShipGroupService;

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImFriendShipGroupMemberService thisService;

//    @Autowired
//    MessageProducer messageProducer;

    @Override
    @Transactional
    public ResponseVO addGroupMember(AddFriendShipGroupMemberReq req) {

        ResponseVO<ImFriendShipGroupEntity> group = imFriendShipGroupService
                .getGroup(req.getFromId(),req.getGroupName(),req.getAppId());
        if(!group.isOk()){
            return group;
        }

        List<String> successId = new ArrayList<>();
        for (String toId : req.getToIds()) {
            ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(toId, req.getAppId());
            if(singleUserInfo.isOk()){
                int i = thisService.doAddGroupMember(group.getData().getGroupId(), toId);
                if(i == 1){
                    successId.add(toId);
                }
            }
        }

        Long seq = imFriendShipGroupService.updateSeq(req.getFromId(), req.getGroupName(), req.getAppId());
//        AddFriendGroupMemberPack pack = new AddFriendGroupMemberPack();
//        pack.setFromId(req.getFromId());
//        pack.setGroupName(req.getGroupName());
//        pack.setToIds(successId);
//        pack.setSequence(seq);
//        messageProducer.sendToUserExceptClient(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_MEMBER_ADD,
//                pack,new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));

        return ResponseVO.successResponse(successId);
    }

    @Override
    public ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req) {
        ResponseVO<ImFriendShipGroupEntity> group = imFriendShipGroupService
                .getGroup(req.getFromId(),req.getGroupName(),req.getAppId());
        if(!group.isOk()){
            return group;
        }

        List<String> successId = new ArrayList<>();
        for (String toId : req.getToIds()) {
            ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(toId, req.getAppId());
            if(singleUserInfo.isOk()){
                int i = deleteGroupMember(group.getData().getGroupId(), toId);
                if(i == 1){
                    successId.add(toId);
                }
            }
        }

        Long seq = imFriendShipGroupService.updateSeq(req.getFromId(), req.getGroupName(), req.getAppId());
//        DeleteFriendGroupMemberPack pack = new DeleteFriendGroupMemberPack();
//        pack.setFromId(req.getFromId());
//        pack.setGroupName(req.getGroupName());
//        pack.setToIds(successId);
//        pack.setSequence(seq);
//        messageProducer.sendToUserExceptClient(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_MEMBER_DELETE,
//                pack,new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));
        return ResponseVO.successResponse(successId);
    }

    @Override
    public int doAddGroupMember(Long groupId, String toId) {
        ImFriendShipGroupMemberEntity imFriendShipGroupMemberEntity = new ImFriendShipGroupMemberEntity();
        imFriendShipGroupMemberEntity.setGroupId(groupId);
        imFriendShipGroupMemberEntity.setToId(toId);

        try {
            int insert = imFriendShipGroupMemberMapper.insert(imFriendShipGroupMemberEntity);
            return insert;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public int deleteGroupMember(Long groupId, String toId) {
        QueryWrapper<ImFriendShipGroupMemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id",groupId);
        queryWrapper.eq("to_id",toId);

        try {
            int delete = imFriendShipGroupMemberMapper.delete(queryWrapper);
//            int insert = imFriendShipGroupMemberMapper.insert(imFriendShipGroupMemberEntity);
            return delete;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int clearGroupMember(Long groupId) {
        QueryWrapper<ImFriendShipGroupMemberEntity> query = new QueryWrapper<>();
        query.eq("group_id",groupId);
        int delete = imFriendShipGroupMemberMapper.delete(query);
        return delete;
    }
}
