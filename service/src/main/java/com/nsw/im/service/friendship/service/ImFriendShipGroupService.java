package com.nsw.im.service.friendship.service;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.nsw.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.nsw.im.service.friendship.model.req.DeleteFriendShipGroupReq;

/**
 * @author nsw
 * @date 2023/9/13 22:07
 */
public interface ImFriendShipGroupService {
    /**
     * 新建分组
     * @param req
     * @return
     */
    public ResponseVO addGroup(AddFriendShipGroupReq req);

    /**
     * 删除分组 同事删除分组下的成员
     * @param req
     * @return
     */
    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req);

    /**
     * 获取好友分组
     * @param fromId
     * @param groupName
     * @param appId
     * @return
     */
    public ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId);

    /**
     *
     * @param fromId
     * @param groupName
     * @param appId
     * @return
     */
    public Long updateSeq(String fromId, String groupName, Integer appId);
}
