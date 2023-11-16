package com.nsw.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.nsw.im.common.BaseErrorCode;
import com.nsw.im.common.config.AppConfig;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.GateWayErrorCode;
import com.nsw.im.common.exception.ApplicationExceptionEnum;
import com.nsw.im.common.utils.SigAPI;
import com.nsw.im.service.user.service.ImUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 接口身份校验类
 * @author nsw
 * @date 2023/11/15 23:03
 */
@Component
public class IdentityCheck {

    private static Logger logger = LoggerFactory.getLogger(IdentityCheck.class);


    @Autowired
    ImUserService imUserService;

    /**
     * 正常情况生产环境是需要一张表来记录密钥的关系，这边先试用配置配置密钥
     */
    @Autowired
    AppConfig appConfig;

    @Autowired
    StringRedisTemplate stringRedisTemplate;


    public ApplicationExceptionEnum checkUserSig(String identifer,
                                                  String appId, String userSig) {

        String cacheUserSig = stringRedisTemplate.opsForValue().get(appId + ":" +Constants.RedisConstants.userSign + ":"
                + identifer + userSig);
        if (!StringUtils.isBlank(cacheUserSig) && Long.valueOf(cacheUserSig)
                > System.currentTimeMillis() / 1000) {
            return BaseErrorCode.SUCCESS;
        }

        // 1、获取密钥
        String privateKey = appConfig.getPrivateKey();

       /* //2、根据appId + 密钥创建sigApi ---静态方法 不需要创建对象
        SigAPI sigAPI = new SigAPI(Long.valueOf(appId), privateKey);*/

        //3、调用sigApi对userSig解密
        JSONObject jsonObject = SigAPI.decodeUserSig(userSig);

        //4、取出解密后的appId 和 操作人 和 过期时间 做匹配，不通过则提示错误
        // 过期时间
        Long expireTime = 0L;
        // 过期秒数
        Long expireSec = 0L;
        // Long time = 0L;
        // 解密后的 appId
        String decoerAppId = "";
        // 解密后的 identifer
        String decoderIdentifer = "";

        try {
            decoerAppId = jsonObject.getString("TLS.appId");
            decoderIdentifer = jsonObject.getString("TLS.identifer");
            // 获取过期秒数
            String expireStr = jsonObject.getString("TLS.expire");
            // 获取过期时间
            String expireTimeStr = jsonObject.getString("TLS.expireTime");
            expireSec = Long.valueOf(expireStr);
            expireTime = Long.valueOf(expireTimeStr) + expireSec;
            // expireTime = time + expireSec;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("checkUserSig-error:{}",e.getMessage());
        }

        if (!decoderIdentifer.equals(identifer)) {
            return GateWayErrorCode.USERSIGN_OPERATE_NOT_MATE;
        }

        if (!decoerAppId.equals(appId)) {
            return GateWayErrorCode.USERSIGN_IS_ERROR;
        }

        if (expireSec == 0L) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        if (expireTime < System.currentTimeMillis()/1000) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        // 把userSig存到redis中
        //key: appid + "xxx" + userId + sign
        String key = appId + ":" +Constants.RedisConstants.userSign + ":"
                + identifer + userSig;
        Long eTime = expireTime - System.currentTimeMillis()/1000;
        stringRedisTemplate.opsForValue().set(key, expireTime.toString(), eTime,
                TimeUnit.SECONDS);

        return BaseErrorCode.SUCCESS;
    }





}
