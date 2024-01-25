package com.nsw.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.nsw.im.codec.pack.LoginPack;
import com.nsw.im.codec.pack.message.ChatMessageAck;
import com.nsw.im.codec.proto.Message;
import com.nsw.im.codec.proto.MessagePack;
import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.ImConnectStatusEnum;
import com.nsw.im.common.enums.command.GroupEventCommand;
import com.nsw.im.common.enums.command.MessageCommand;
import com.nsw.im.common.enums.command.SystemCommand;
import com.nsw.im.common.model.UserClientDto;
import com.nsw.im.common.model.UserSession;
import com.nsw.im.common.model.message.CheckSendMessageReq;
import com.nsw.im.tcp.feign.FeignMessageService;
import com.nsw.im.tcp.publish.MqMessageProducer;
import com.nsw.im.tcp.redis.RedisManager;
import com.nsw.im.tcp.server.ImServer;
import com.nsw.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Attribute;
import java.net.InetAddress;

/**
 * @author nsw
 * @date 2023/9/28 22:47
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private Integer brokerId;

    private String logicUrl;

    private FeignMessageService feignMessageService;

    public NettyServerHandler(Integer brokerId ,String logicUrl) {
        this.brokerId = brokerId;
        feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 3500))//设置超时时间
                .target(FeignMessageService.class, logicUrl);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {

        Integer command = message.getMessageHeader().getCommand();
        // 登录command
        if (command == SystemCommand.LOGIN.getCommand()) {

            LoginPack loginPack = JSON.parseObject(JSONObject.toJSONString(message.getMessagePack()),
                    new TypeReference<LoginPack>() {
                    }.getType());

            //为channel设置userId,appId,clientType属性
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(loginPack.getUserId());
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(message.getMessageHeader().getAppId());
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.ClientType)).set(message.getMessageHeader().getClientType());
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.Imei)).set(message.getMessageHeader().getImei());

            //将channel存起来

            //用redis map存session
            UserSession userSession = new UserSession();
            userSession.setAppId(message.getMessageHeader().getAppId());
            userSession.setClientType(message.getMessageHeader().getClientType());
            userSession.setUserId(loginPack.getUserId());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            userSession.setBrokerId(brokerId);
            userSession.setImei(message.getMessageHeader().getImei());
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                userSession.setBrokerHost(localHost.getHostAddress());
            }catch (Exception e){
                e.printStackTrace();
            }

            //存到redis
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(message.getMessageHeader().getAppId()
                    + Constants.RedisConstants.UserSessionConstants
                    + loginPack.getUserId());
            map.put(message.getMessageHeader().getClientType() + ":" + message.getMessageHeader().getImei(),
                    JSONObject.toJSONString(userSession));

            SessionSocketHolder
                     .put(message.getMessageHeader().getAppId(), loginPack.getUserId(),
                             message.getMessageHeader().getClientType(), message.getMessageHeader().getImei(),
                             (NioSocketChannel)channelHandlerContext.channel());


            //使用redis的发布订阅模式，处理多端登录的逻辑
            UserClientDto dto = new UserClientDto();
            dto.setImei(message.getMessageHeader().getImei());
            dto.setUserId(loginPack.getUserId());
            dto.setClientType(message.getMessageHeader().getClientType());
            dto.setAppId(message.getMessageHeader().getAppId());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSONObject.toJSONString(dto));

        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            // 退出登录：
//            // 1.删除session
//            String userId = (String) channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.UserId)).get();
//            Integer appId = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.AppId)).get();
//            Integer clientType = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.ClientType)).get();
//
//            SessionSocketHolder.remove(appId, userId, clientType);
//            // 2.redis 删除
//            RedissonClient redissonClient = RedisManager.getRedissonClient();
//            RMap<Object, Object> map = redissonClient.getMap(appId
//                    + Constants.RedisConstants.UserSessionConstants
//                    + userId);
//            map.remove(clientType);
//            // 3.关闭channel
//            channelHandlerContext.channel().close();
            // 上面的逻辑封装到方法中
            SessionSocketHolder.removeUserSession((NioSocketChannel) channelHandlerContext.channel());
        } else if (command == SystemCommand.PING.getCommand()) {
            // 将最后一次读写事件的时间设置到handler
            channelHandlerContext.channel()
                    .attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());

        }
        else if (command == MessageCommand.MSG_P2P.getCommand()
                || command == GroupEventCommand.MSG_GROUP.getCommand()) {
            // 发送消息跑p2p消息和群组消息校验合法性
            try {
                String toId = "";
                CheckSendMessageReq req = new CheckSendMessageReq();
                req.setAppId(message.getMessageHeader().getAppId());
                req.setCommand(message.getMessageHeader().getCommand());
                JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(message.getMessagePack()));
                String fromId = jsonObject.getString("fromId");
                if(command == MessageCommand.MSG_P2P.getCommand()){
                    toId = jsonObject.getString("toId");
                }else {
                    toId = jsonObject.getString("groupId");
                }
                req.setToId(toId);
                req.setFromId(fromId);
                // 调用校验消息发送方的接口 (使用feign进行rpc调用)
                ResponseVO responseVO = feignMessageService.checkSendMessage(req);
                if(responseVO.isOk()){
                    // 校验成功 投递消息到mq
                    MqMessageProducer.sendMessage(message, command);
                }else{
                    // 如果失败直接ack
                    Integer ackCommand = 0;
                    if(command == MessageCommand.MSG_P2P.getCommand()){
                        ackCommand = MessageCommand.MSG_ACK.getCommand();
                    }else {
                        ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                    }

                    ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                    responseVO.setData(chatMessageAck);
                    MessagePack<ResponseVO> ack = new MessagePack<>();
                    ack.setData(responseVO);
                    ack.setCommand(ackCommand);
                    channelHandlerContext.channel().writeAndFlush(ack);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            // 发送mq消息到逻辑层
            MqMessageProducer.sendMessage(message, command);
        }

    }

    /**
     * 表示 channel 处于不活跃状态
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //channel处于不活跃状态 设置用户的session为离线状态
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
        ctx.close();
    }

    /**
     * 当心跳检测的handler检测到超时之后会调下一个handler的userEventTriggered方法
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }
}
