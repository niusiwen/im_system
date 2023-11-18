package com.nsw.im.service.message.service;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.config.AppConfig;
import com.nsw.im.common.enums.*;
import com.nsw.im.service.friendship.dao.ImFriendShipEntity;
import com.nsw.im.service.friendship.model.req.GetRelationReq;
import com.nsw.im.service.friendship.service.ImFriendService;
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
    AppConfig appConfig;

    /**
     * 校验发送方是否满足发送条件
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

}
