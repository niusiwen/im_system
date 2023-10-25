package com.nsw.im.service.friendship.service;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.nsw.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;

/**
 * @author nsw
 * @date 2023/9/13 22:07
 */
public interface ImFriendShipGroupMemberService {

    /**
     * 添加分组成员
     * @param req
     * @return
     */
    public ResponseVO addGroupMember(AddFriendShipGroupMemberReq req);

    /**
     * 删除分组成员
     * @param req
     * @return
     */
    public ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req);


    /**
     *
     * @param groupId
     * @param toId
     * @return
     */
    public int doAddGroupMember(Long groupId, String toId);


    public int clearGroupMember(Long groupId);
}
