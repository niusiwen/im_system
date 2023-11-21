package com.nsw.im.tcp.feign;

import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.model.message.CheckSendMessageReq;
import feign.Headers;
import feign.RequestLine;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
public interface FeignMessageService {

    @Headers({"Content-Type: application/json","Accept: application/json"})
    @RequestLine("POST /message/checkSend")
    public ResponseVO checkSendMessage(CheckSendMessageReq o);

}
