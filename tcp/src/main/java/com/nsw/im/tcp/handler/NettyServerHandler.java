package com.nsw.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.nsw.im.codec.pack.LoginPack;
import com.nsw.im.codec.proto.Message;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.ImConnectStatusEnum;
import com.nsw.im.common.enums.command.SystemCommand;
import com.nsw.im.common.model.UserClientDto;
import com.nsw.im.common.model.UserSession;
import com.nsw.im.tcp.redis.RedisManager;
import com.nsw.im.tcp.server.ImServer;
import com.nsw.im.tcp.utils.SessionSocketHolder;
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

    public NettyServerHandler(Integer brokerId) {
        this.brokerId = brokerId;
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
            map.put(message.getMessageHeader().getClientType() + "：" + message.getMessageHeader().getImei(),
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
