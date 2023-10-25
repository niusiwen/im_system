package com.nsw.im.service.utils;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.config.AppConfig;
import com.nsw.im.common.utils.HttpRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nsw
 * @date 2023/10/19 22:31
 */
@Component
public class CallbackService {

    private Logger logger = LoggerFactory.getLogger(CallbackService.class);

    @Autowired
    HttpRequestUtils httpRequestUtils;

    @Autowired
    AppConfig appConfig;

    /**
     * 业务之后的回调
     * @param appId   应用id
     * @param callbackCommand 指令
     * @param jsonBody 请求体
     */
    public void callback(Integer appId, String callbackCommand, String jsonBody) {

        try {
            httpRequestUtils.doPost("", Object.class, builderUrlParams(appId, callbackCommand),
                    jsonBody, null);
        } catch (Exception e) {
            // e.printStackTrace();
            logger.error("callback 回调{} ：{} 出现异常：{}", callbackCommand, appId, e.getLocalizedMessage());
        }
    }

    /**
     * 业务之前的回调,需要返回的结果
     * @param appId
     * @param callbackCommand
     * @param jsonBody
     */
    public ResponseVO beforeCallback(Integer appId, String callbackCommand, String jsonBody) {

        try {
            ResponseVO responseVO = httpRequestUtils.doPost("", ResponseVO.class, builderUrlParams(appId, callbackCommand),
                    jsonBody, null);
            return responseVO;
        } catch (Exception e) {
            // e.printStackTrace();
            logger.error("callback 之前 回调{} ：{} 出现异常：{}", callbackCommand, appId, e.getLocalizedMessage());
            return ResponseVO.successResponse();
        }
    }


    public Map builderUrlParams(Integer appId, String command) {
        Map map = new HashMap();
        map.put("appId", appId);
        map.put("command", command);
        return map;
    }

}
