package com.nsw.im.service.message.service;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.config.AppConfig;
import com.nsw.im.common.enums.*;
import com.nsw.im.service.friendship.dao.ImFriendShipEntity;
import com.nsw.im.service.friendship.model.req.GetRelationReq;
import com.nsw.im.service.friendship.service.ImFriendService;
import com.nsw.im.service.group.dao.ImGroupEntity;
import com.nsw.im.service.group.model.resp.GetRoleInGroupResp;
import com.nsw.im.service.group.service.ImGroupMemberService;
import com.nsw.im.service.group.service.ImGroupService;
import com.nsw.im.service.user.dao.ImUserDataEntity;
import com.nsw.im.service.user.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author nsw
 * @date 2023/11/18 10:58
 */
@Service
public class CheckSendMessageService {

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImFriendService imFriendService;
    
    @Autowired
    ImGroupService imGroupService;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    AppConfig appConfig;

    /**
     * 校验发送方是否满足发送条件（是否被禁用，是否被禁言）
     * @param fromId
     * @param appId
     * @return
     */
    public ResponseVO checkSenderForbidAndMute(String fromId, Integer appId){

        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(fromId, appId);
        if(!singleUserInfo.isOk()){
            return singleUserInfo;
        }

        ImUserDataEntity user = singleUserInfo.getData();
        if(user.getForbiddenFlag() == UserForbiddenFlagEnum.FORBIBBEN.getCode()){
            // 发送方被禁用
            return  ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_FORBIBBEN);
        } else if (user.getSilentFlag() == UserSilentFlagEnum.MUTE.getCode()) {
            // 发送方被禁言
            return  ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_MUTE);
        }

        return ResponseVO.successResponse();
    }

    /**
     * 校验好友管理链，是否是好友 是否拉黑
     * @param fromId
     * @param toId
     * @param appId
     * @return
     */
    public ResponseVO checkFriendShip(String fromId, String toId, Integer appId) {

        // 是否校验好友关系
        if (appConfig.isSendMessageCheckFriend()) {
            GetRelationReq fromReq = new GetRelationReq();
            fromReq.setFromId(fromId);
            fromReq.setToId(toId);
            fromReq.setAppId(appId);
            ResponseVO<ImFriendShipEntity> fromRelation = imFriendService.getRelation(fromReq);
            if (!fromRelation.isOk()) {
                return fromRelation;
            }

            GetRelationReq toReq = new GetRelationReq();
            toReq.setFromId(toId);
            toReq.setToId(fromId);
            toReq.setAppId(appId);
            ResponseVO<ImFriendShipEntity> toRelation = imFriendService.getRelation(toReq);
            if (!toRelation.isOk()) {
                return toRelation;
            }

            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()
                    != fromRelation.getData().getStatus()) {
                // 把对方删掉了
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()
                    != toRelation.getData().getStatus()) {
                // 对方把你删掉
                return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_DELETED_YOU);
            }

            // 是否是黑名单
            if (appConfig.isSendMessageCheckBlack()) {

                if (FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()
                        != fromRelation.getData().getBlack()) {
                    // 把对方拉黑了
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
                }

                if (FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()
                        != toRelation.getData().getBlack()) {
                    //对方把你拉黑了
                    return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_BLACK_YOU);
                }

            }

        }
        return ResponseVO.successResponse();
    }

    /**
     * 判断群消息的合法性
     * @param fromId
     * @param appId
     * @return
     */
    public ResponseVO checkGroupMessage(String fromId, String groupId, Integer appId) {
        // 判断发送方是否满足条件
        ResponseVO responseVO = checkSenderForbidAndMute(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }

        // 判断群逻辑 群是否存在
        ResponseVO<ImGroupEntity> group = imGroupService.getGroup(groupId, appId);
        if (!group.isOk()) {
            return group;
        }

        // 判断群成员是否在群里
        ResponseVO<GetRoleInGroupResp> roleInGroupOne = imGroupMemberService.getRoleInGroupOne(groupId, fromId, appId);
        if (!roleInGroupOne.isOk()) {
            return roleInGroupOne;
        }

        // 判断群是否被禁言，如果禁言只有群管理和群主可以发言
        ImGroupEntity groupData = group.getData();
        GetRoleInGroupResp data = roleInGroupOne.getData();
        if(groupData.getMute() == GroupMuteTypeEnum.MUTE.getCode()
                && !(data.getRole() == GroupMemberRoleEnum.MAMAGER.getCode()
                || data.getRole() == GroupMemberRoleEnum.OWNER.getCode())) {
            return ResponseVO.errorResponse(GroupErrorCode.THIS_GROUP_IS_MUTE);
        }

        // 个人在群内是否被禁言
        if (data.getSpeakDate() != null && data.getSpeakDate() > System.currentTimeMillis()) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MEMBER_IS_SPEAK);
        }

        return ResponseVO.successResponse();
    }

}
