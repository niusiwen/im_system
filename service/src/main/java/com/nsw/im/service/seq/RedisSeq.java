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

    /**
     * 生成全局唯一的自增序列号
     * @param key
     * @return
     */
    public long doGetSeq(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }
}
