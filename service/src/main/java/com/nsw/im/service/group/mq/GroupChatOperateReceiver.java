package com.nsw.im.service.group.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.command.GroupEventCommand;
import com.nsw.im.common.enums.command.MessageCommand;
import com.nsw.im.common.model.message.GroupChatMessageContent;
import com.nsw.im.service.group.service.GroupMessageService;
import com.nsw.im.service.message.service.P2PMessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 订阅从tcp服务投递过来的 群消息的接收器
 * @author nsw
 * @date 2023/11/16 22:55
 */
@Component
@Slf4j
public class GroupChatOperateReceiver {

//    @Autowired
//    P2PMessageService p2PMessageService;

    @Autowired
    GroupMessageService groupMessageService;

    @RabbitListener(
            // 绑定交换机和消息队列
            bindings = @QueueBinding(
                    // 队列名，是否持久化
                    value = @Queue(value = Constants.RabbitConstants.Im2GroupService, durable = "true" ),
                    // 交换机名，是否持久化
                    exchange = @Exchange(value = Constants.RabbitConstants.Im2GroupService, durable = "true")
            ),
            // 每次从队列中拉取多少消息
            concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String, Object> headers,
                              Channel channel) throws Exception {

        String msg = new String(message.getBody(), "utf-8");
        log.info("CHAT MSG FROM QUEUE ::: {}", msg);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");

            if (command.equals(GroupEventCommand.MSG_GROUP.getCommand())) {
                // 处理群消息
                GroupChatMessageContent messageContent = jsonObject.toJavaObject(GroupChatMessageContent.class);
//                p2PMessageService.process(messageContent);

                groupMessageService.process(messageContent);

            }
            // 处理完消息，ack确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理消息出现异常：{}", e.getMessage());
            log.error("RMQ_CHAT_TRAN_ERROR", e);
            log.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        }


    }

}
