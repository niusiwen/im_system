package com.nsw.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.ImConnectStatusEnum;
import com.nsw.im.common.model.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 获取Redis中userSession的工具类
 * @author nsw
 * @date 2023/11/3 19:52
 */
@Service
public class UserSessionUtils {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //1、获取用户所有的session

    public List<UserSession> getUserSession(Integer appId, String userId) {

        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants
                + userId;

        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(userSessionKey);

        List<UserSession> list = new ArrayList<>();
        Collection<Object> values = entries.values();
        for (Object o : values) {
            String str = (String)o;
            UserSession userSession = JSONObject.parseObject(str, UserSession.class);
            if (userSession.getConnectState() == ImConnectStatusEnum.ONLINE_STATUS.getCode()) {
                list.add(userSession);
            }
        }
        return list;
    }

    //2、获取用户除了本端的session


    //3、 获取用户指定端的session
    public UserSession getUserSession(Integer appId, String userId,
                                       Integer clientType, String imei ) {

        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants
                + userId;

        String hashKey = clientType + ":" +imei;

        Object o = stringRedisTemplate.opsForHash().get(userSessionKey, hashKey);

        UserSession userSession = JSONObject.parseObject(o.toString(), UserSession.class);
        return userSession;
    }




}
