package com.nsw.im.service.seq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author nsw
 * @date 2024/1/25 19:02
 */
@Service
public class RedisSeq {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public long deGetSeq(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }
}
