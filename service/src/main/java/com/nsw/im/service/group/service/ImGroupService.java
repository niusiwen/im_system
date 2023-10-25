package com.nsw.im.service.group.service;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.service.group.dao.ImGroupEntity;
import com.nsw.im.service.group.model.req.*;

/**
 * @author nsw
 * @date 2023/9/22 22:16
 */
public interface ImGroupService {

    public ResponseVO importGroup(ImportGroupReq req);

    public ResponseVO createGroup(CreateGroupReq req);

    public ResponseVO updateBaseGroupInfo(UpdateGroupReq req);

    public ResponseVO getJoinedGroup(GetJoinedGroupReq req);

    public ResponseVO destroyGroup(DestroyGroupReq req);

    public ResponseVO transferGroup(TransferGroupReq req);

    public ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId);

    public ResponseVO getGroup(GetGroupReq req);

    public ResponseVO muteGroup(MuteGroupReq req);

//    ResponseVO syncJoinedGroupList(SyncReq req);

    Long getUserGroupMaxSeq(String userId, Integer appId);
}
