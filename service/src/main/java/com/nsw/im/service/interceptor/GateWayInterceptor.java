package com.nsw.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.nsw.im.common.BaseErrorCode;
import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.enums.GateWayErrorCode;
import com.nsw.im.common.exception.ApplicationExceptionEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 接口网关拦截器
 * @author nsw
 * @date 2023/11/15 22:50
 */
@Component
public class GateWayInterceptor implements HandlerInterceptor {

    @Autowired
    IdentityCheck identityCheck;

    /**
     * userSig 生成：
     */

    /**
     * 请求接口之前，进行接口身份验证
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 获取appId 操作人 签名数据userSign
        String appIdStr = request.getParameter("appId");
        if(StringUtils.isBlank(appIdStr)){
            resp(ResponseVO.errorResponse(GateWayErrorCode.APPID_NOT_EXIST),
                    response);
            return false;
        }

        String identifier = request.getParameter("identifier");
        if(StringUtils.isBlank(identifier)){
            resp(ResponseVO.errorResponse(GateWayErrorCode.OPERATER_NOT_EXIST),
                    response);
            return false;
        }

        String userSign = request.getParameter("userSign");
        if(StringUtils.isBlank(userSign)){
            resp(ResponseVO.errorResponse(GateWayErrorCode.USERSIGN_NOT_EXIST),
                    response);
            return false;
        }

        // 校验签名、操作人 和 appId 是否匹配
        ApplicationExceptionEnum applicationExceptionEnum = identityCheck.checkUserSig(identifier, appIdStr, userSign);
        if (applicationExceptionEnum != BaseErrorCode.SUCCESS) {
            resp(ResponseVO.errorResponse(applicationExceptionEnum),
                    response);
            return false;
        }

        return true;
    }


    private void resp(ResponseVO respVO, HttpServletResponse response){

        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");

        try {
            String resp = JSONObject.toJSONString(respVO);
            writer = response.getWriter();
            writer.write(resp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }
}
