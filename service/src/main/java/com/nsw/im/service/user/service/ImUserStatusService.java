package com.nsw.im.service.user.service;

import com.nsw.im.service.user.model.req.PullFriendOnlineStatusReq;
import com.nsw.im.service.user.model.req.PullUserOnlineStatusReq;
import com.nsw.im.service.user.model.req.SetUserCustomerStatusReq;
import com.nsw.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.nsw.im.service.user.model.resp.UserOnlineStatusResp;

import java.util.Map;

/**
 * @author nsw
 * @date 2023/8/8 21:55
 */
public interface ImUserStatusService {

    // public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

    void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

    void setUserCustomerStatus(SetUserCustomerStatusReq req);

    Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req);

    Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req);

}
