package com.nsw.im.tcp.redis;

import com.nsw.im.codec.config.BootstrapConfig;
import com.nsw.im.tcp.reciver.UserLoginMessageListener;
import org.redisson.api.RedissonClient;

/**
 * @author nsw
 * @date 2023/9/30 16:30
 */
public class RedisManager {

    private static RedissonClient redissonClient;

    private static Integer loginModel;

    public static void init(BootstrapConfig config) {
        loginModel = config.getNim().getLoginModel();
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getNim().getRedis());

        // 初始化监听类
        UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(loginModel);
        userLoginMessageListener.ListenerUserLogin();
    }

    public static RedissonClient getRedissonClient() {
        return redissonClient;
    }
}
