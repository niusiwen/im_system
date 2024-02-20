package com.nsw.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.command.MessageCommand;
import com.nsw.im.common.model.message.MessageContent;
import com.nsw.im.common.model.message.MessageReadedContent;
import com.nsw.im.common.model.message.MessageReceiveAckContent;
import com.nsw.im.service.message.service.MessageSyncService;
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
 * 订阅从tcp服务投递过来的消息的接收器类
 * @author nsw
 * @date 2023/11/16 22:55
 */
@Component
@Slf4j
public class ChatOperateReceiver {

    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    @RabbitListener(
            // 绑定交换机和消息队列
            bindings = @QueueBinding(
                    // 队列名，是否持久化
                    value = @Queue(value = Constants.RabbitConstants.Im2MessageService, durable = "true" ),
                    // 交换机名，是否持久化，没有配置type默认是direct类型
                    exchange = @Exchange(value = Constants.RabbitConstants.Im2MessageService, durable = "true")
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

            if (command.equals(MessageCommand.MSG_P2P.getCommand())) {
                // 处理消息
                MessageContent messageContent = jsonObject.toJavaObject(MessageContent.class);
                p2PMessageService.process(messageContent);

            } else if(command.equals(MessageCommand.MSG_RECEIVE_ACK.getCommand())) {
                // 消息接收确认
                MessageReceiveAckContent messageContent = jsonObject.toJavaObject(MessageReceiveAckContent.class);
                messageSyncService.receiveMark(messageContent);

            } else if(command.equals(MessageCommand.MSG_READED.getCommand())) {
                // 消息已读
                MessageReadedContent messageContent = jsonObject.toJavaObject(MessageReadedContent.class);
                messageSyncService.readMark(messageContent);

            }
            //处理完消息，ack确认
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
