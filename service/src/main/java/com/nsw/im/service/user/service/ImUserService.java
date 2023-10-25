package com.nsw.im.service.user.service;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.service.user.dao.ImUserDataEntity;
import com.nsw.im.service.user.model.req.*;
import com.nsw.im.service.user.model.resp.GetUserInfoResp;

/**
 * @author nsw
 * @date 2023/8/8 21:50
 */
public interface ImUserService {

    public ResponseVO importUser(ImportUserReq req);

    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId , Integer appId);

    public ResponseVO deleteUser(DeleteUserReq req);

    public ResponseVO modifyUserInfo(ModifyUserInfoReq req);

    public ResponseVO login(LoginReq req);

    ResponseVO getUserSequence(GetUserSequenceReq req);
}
