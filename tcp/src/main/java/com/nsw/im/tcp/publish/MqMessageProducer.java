package com.nsw.im.tcp.publish;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nsw.im.codec.proto.Message;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.tcp.utils.MqFactory;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * tcp服务发送MQ消息的类
 * @author nsw
 * @date 2023/10/8 21:29
 */
@Slf4j
public class MqMessageProducer {

    public static void sendMessage(Message message, Integer command) {
        Channel channel = null;
        String channelName = Constants.RabbitConstants.Im2MessageService;
        if (command.toString().startsWith("2")) {
            channelName = Constants.RabbitConstants.Im2GroupService;
        }
        try {
            channel = MqFactory.getChannel(channelName);
            JSONObject jsonObject = (JSONObject) JSON.toJSON(message.getMessagePack());
            jsonObject.put("command", command);
            jsonObject.put("clientType", message.getMessageHeader().getClientType());
            jsonObject.put("appId", message.getMessageHeader().getAppId());
            jsonObject.put("imei", message.getMessageHeader().getImei());
            /**
             * 发送mq消息，
             * 参数1：exchangeName 同channelName,这里传入channelName
             * 参数2: routingKey
             * 参数3：BasicProperties 消息属性
             * 参数4：消息体，字节数组
             */
            channel.basicPublish(channelName, "",
                    null, jsonObject.toJSONString().getBytes());
        }catch (Exception e) {
            log.error("发送出现异常：{}", e.getMessage());
        }
    }
}
