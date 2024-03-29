package com.nsw.im.messagestore.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.messagestore.dao.ImMessageBodyEntity;
import com.nsw.im.messagestore.model.DoStoreP2PMessageDto;
import com.nsw.im.messagestore.service.StoreMessageService;
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
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author nsw
 * @date 2023/12/2 11:14
 */
@Slf4j
@Service
public class StoreP2PMessageReceiver {

    @Autowired
    StoreMessageService storeMessageService;


    @RabbitListener(
            // 绑定交换机和消息队列
            bindings = @QueueBinding(
                    // 队列名，是否持久化
                    value = @Queue(value = Constants.RabbitConstants.StoreP2PMessage, durable = "true" ),
                    // 交换机名，是否持久化，没有配置type默认是direct类型
                    exchange = @Exchange(value = Constants.RabbitConstants.StoreP2PMessage, durable = "true")
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

            DoStoreP2PMessageDto doStoreP2PMessageDto = jsonObject.toJavaObject(DoStoreP2PMessageDto.class);
            // 传入的是common包下的ImMessageBody，需要转成messageStore下的ImMessageBodyEntity
            ImMessageBodyEntity messageBody = jsonObject.getObject("messageBody", ImMessageBodyEntity.class);
            doStoreP2PMessageDto.setMessageBody(messageBody);
            storeMessageService.doStoreP2PMessage(doStoreP2PMessageDto);

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
