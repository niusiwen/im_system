package com.nsw.im.service.friendship.service;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.model.RequestBase;
import com.nsw.im.service.friendship.model.req.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author nsw
 * @date 2023/8/12 9:51
 */
public interface ImFriendService {

    /**
     * 批量导入好友关系
     * @param req
     * @return
     */
    public ResponseVO importFriendShip(ImportFriendShipReq req);

    public ResponseVO addFriend(AddFriendReq req);

    public ResponseVO updateFriend(UpdateFriendReq req);

    public ResponseVO deleteFriend(DeleteFriendReq req);

    public ResponseVO deleteAllFriend(DeleteFriendReq req);

    public ResponseVO getAllFriendShip(GetAllFriendShipReq req);

    public ResponseVO getRelation(GetRelationReq req);

    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId);

    public ResponseVO checkFriendship(CheckFriendShipReq req);

    public ResponseVO addBlack(AddFriendShipBlackReq req);

    public ResponseVO deleteBlack(DeleteBlackReq req);

    public ResponseVO checkBlack(CheckFriendShipReq req);

    // public ResponseVO syncFriendshipList(SyncReq req);

    public List<String> getAllFriendId(String userId, Integer appId);
}
