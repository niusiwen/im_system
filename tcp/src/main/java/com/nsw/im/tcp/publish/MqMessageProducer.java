package com.nsw.im.tcp.publish;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nsw.im.codec.proto.Message;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.tcp.utils.MqFactory;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * 发送消息的类
 * @author nsw
 * @date 2023/10/8 21:29
 */
@Slf4j
public class MqMessageProducer {

    public static void sendMessage(Message message, Integer command) {
        Channel channel = null;
        String channelName = Constants.RabbitConstants.Im2MessageService;
        try {
            channel = MqFactory.getChannel(channelName);
            JSONObject jsonObject = (JSONObject) JSON.toJSON(message.getMessagePack());
            jsonObject.put("command", command);
            jsonObject.put("clientType", message.getMessageHeader().getClientType());
            jsonObject.put("appId", message.getMessageHeader().getAppId());
            jsonObject.put("imei", message.getMessageHeader().getImei());

            channel.basicPublish(channelName, "",
                    null, jsonObject.toJSONString().getBytes());
        }catch (Exception e) {
            log.error("发送出现异常：{}", e.getMessage());
        }
    }
}
