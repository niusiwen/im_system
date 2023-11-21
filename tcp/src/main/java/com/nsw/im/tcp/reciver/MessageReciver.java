package com.nsw.im.tcp.reciver;

import com.alibaba.fastjson.JSONObject;
import com.nsw.im.codec.proto.MessagePack;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.tcp.reciver.process.BaseProcess;
import com.nsw.im.tcp.reciver.process.ProcessFactory;
import com.nsw.im.tcp.utils.MqFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author nsw
 * @date 2023/10/8 21:40
 */
@Slf4j
public class MessageReciver {

    private static String brokerId;

    public static void startReciverMessage() {
        try {
            Channel channel = MqFactory
                    .getChannel(Constants.RabbitConstants.MessageService2Im + brokerId);

            /**
             * 声明交换机，
             */
             channel.exchangeDeclare(Constants.RabbitConstants.MessageService2Im, "topic", true, false, false, null);
            /**
             * 1、channel声明队列
             * 第一个参数：queueName
             * 第二个参数：是否持久化
             * 第三个参数：独占，一般为false
             * 第四个参数：是否删除
             * 第五个参数：额外参数
             */
            channel.queueDeclare(Constants.RabbitConstants.MessageService2Im + brokerId,
                    true, false, false, null);
            /**
             * 2、channel绑定转换机和队列
             * 参数1：queueName
             * 参数2：exchangeName
             * 参数3：routingKey
             */
            channel.queueBind(Constants.RabbitConstants.MessageService2Im + brokerId,
                    Constants.RabbitConstants.MessageService2Im, brokerId);
            /**
             * 3、channel监听消息
             * 第一个参数：queueName
             * 第二个参数：是否自动提交，一般为false
             */
            channel.basicConsume(Constants.RabbitConstants.MessageService2Im + brokerId, false,
                    new DefaultConsumer(channel){
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            // super.handleDelivery(consumerTag, envelope, properties, body);
                            // todo 处理消息服务发来的消息
                            try {
                                String msgStr = new String(body);
                                MessagePack pack = JSONObject.parseObject(msgStr, MessagePack.class);
                                // 拿到处理器对象
                                BaseProcess messageProcess = ProcessFactory.getMessageProcess(pack.getCommand());
                                messageProcess.process(pack);
                                log.info(msgStr);
                                // 确认消息被消费
                                /**
                                 * 第一个参数：消息的标记符
                                 * 第二个参数：消息是否是批量提交
                                 */
                                channel.basicAck(envelope.getDeliveryTag(), false);
                            } catch (IOException e) {
                                e.printStackTrace();
                                // 前两个参数与basicAck方法一样，第三个参数是否重回队列
                                channel.basicNack(envelope.getDeliveryTag(), false, false);
                            }

                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        startReciverMessage();
    }

    public static void init(String brokerId) {
        if (StringUtils.isBlank(MessageReciver.brokerId)) {
            MessageReciver.brokerId = brokerId;
        }
        startReciverMessage();
    }

}
