package com.nsw.im.tcp.publish;

import com.alibaba.fastjson.JSONObject;
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

    public static void sendMessage(Object message) {
        Channel channel = null;
        String channelName = "";
        try {
            channel = MqFactory.getChannel(channelName);
            channel.basicPublish(channelName, "",
                    null, JSONObject.toJSONString(message).getBytes());
        }catch (Exception e) {
            log.error("发送出现异常：{}", e.getMessage());
        }
    }
}
