package com.nsw.im.tcp.utils;

import com.alibaba.fastjson.JSONObject;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.ImConnectStatusEnum;
import com.nsw.im.common.model.UserClientDto;
import com.nsw.im.common.model.UserSession;
import com.nsw.im.tcp.redis.RedisManager;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author nsw
 * @date 2023/9/29 17:42
 */
public class SessionSocketHolder {

    private static final Map<UserClientDto, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();


    public static void put(Integer appId, String userId, Integer clientType, String imei,
                           NioSocketChannel channel) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setImei(imei);
        userClientDto.setAppId(appId);
        userClientDto.setUserId(userId);
        userClientDto.setClientType(clientType);

        CHANNELS.put(userClientDto, channel);
    }

    public static NioSocketChannel get(Integer appId, String userId, Integer clientType, String imei) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setImei(imei);
        userClientDto.setAppId(appId);
        userClientDto.setUserId(userId);
        userClientDto.setClientType(clientType);
        return CHANNELS.get(userClientDto);
    }

    /**
     * 获取所有的NioSocketChannel
     * @param appId
     * @param id
     * @return
     */
    public static List<NioSocketChannel> get(Integer appId , String id) {

        Set<UserClientDto> channelInfos = CHANNELS.keySet();
        List<NioSocketChannel> channels = new ArrayList<>();

        channelInfos.forEach(channel ->{
            if(channel.getAppId().equals(appId) && id.equals(channel.getUserId())){
                channels.add(CHANNELS.get(channel));
            }
        });

        return channels;
    }

    public static void remove(Integer appId, String userId, Integer clientType, String imei) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setImei(imei);
        userClientDto.setAppId(appId);
        userClientDto.setUserId(userId);
        userClientDto.setClientType(clientType);
        CHANNELS.remove(userClientDto);
    }


    public static void remove(NioSocketChannel channel) {
        CHANNELS.entrySet().stream().filter(entry -> entry.getValue() == channel)
                .forEach(entry -> CHANNELS.remove(entry.getKey()));
    }

    /**
     * 删除用户session
     * @param nioSocketChannel
     */
    public static void removeUserSession(NioSocketChannel nioSocketChannel) {
        // 1.删除session
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();


        SessionSocketHolder.remove(appId, userId, clientType, imei);
        // 2.redis 删除
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId
                + Constants.RedisConstants.UserSessionConstants
                + userId);
        map.remove(clientType + ":" + imei);
        // 3.关闭channel
        nioSocketChannel.close();
    }

    /**
     * 用户离线
     * @param nioSocketChannel
     */
    public static void offlineUserSession(NioSocketChannel nioSocketChannel) {
        // 1.删除session
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

        SessionSocketHolder.remove(appId, userId, clientType, imei);
        // 2.redis 修改状态为OFFLINE
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId
                + Constants.RedisConstants.UserSessionConstants
                + userId);
        String sessionStr = (String) map.get(clientType+":"+imei);

        if(!StringUtils.isBlank(sessionStr)) {
            UserSession userSession = JSONObject.parseObject(sessionStr, UserSession.class);
            userSession.setConnectState(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
            //key 为终端类型 + 设备号
            map.put(clientType+":"+imei, JSONObject.toJSONString(userSession));
        }
        // 3.关闭channel
        nioSocketChannel.close();
    }

}
