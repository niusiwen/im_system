package com.nsw.im.service.utils;

import com.nsw.im.common.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author nsw
 * @date 2024/3/2 16:04
 */
@Service
public class WriteUserSeq {

    // 序列号使用redis 的hash存储

    @Autowired
    RedisTemplate redisTemplate;


    /**
     * 记录用户的seq，用户客户端增量拉取数据
     * @param appId
     * @param userId
     * @param type
     * @param seq
     */
    public void writeUserSeq(Integer appId, String userId, String type, Long seq) {
        String key = appId + ":" + Constants.RedisConstants.SeqPrefix + ":" + userId;
        redisTemplate.opsForHash().put(key, type, seq);
    }



}
