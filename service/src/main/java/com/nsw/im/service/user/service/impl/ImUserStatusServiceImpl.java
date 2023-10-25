package com.nsw.im.service.user.service.impl;

import com.nsw.im.service.user.model.req.PullFriendOnlineStatusReq;
import com.nsw.im.service.user.model.req.PullUserOnlineStatusReq;
import com.nsw.im.service.user.model.req.SetUserCustomerStatusReq;
import com.nsw.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.nsw.im.service.user.model.resp.UserOnlineStatusResp;
import com.nsw.im.service.user.service.ImUserStatusService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author nsw
 * @date 2023/8/8 21:55
 */
@Service
public class ImUserStatusServiceImpl implements ImUserStatusService {


    @Override
    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req) {

    }

    @Override
    public void setUserCustomerStatus(SetUserCustomerStatusReq req) {

    }

    @Override
    public Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req) {
        return null;
    }

    @Override
    public Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req) {
        return null;
    }
}
