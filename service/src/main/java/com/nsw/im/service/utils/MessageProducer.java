package com.nsw.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.nsw.im.codec.proto.MessagePack;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.command.Command;
import com.nsw.im.common.model.ClientInfo;
import com.nsw.im.common.model.UserSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 给用户发送消息的工具类
 * @author nsw
 * @date 2023/11/3 22:26
 */
@Service
public class MessageProducer {

    private static Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    UserSessionUtils userSessionUtils;

    /**
     * 队列名称
     */
    private String queueName = Constants.RabbitConstants.MessageService2Im;

    /**
     * 服务端发送mq消息
     * @param session
     * @param msg
     * @return
     */
    public boolean sendMessage(UserSession session, Object msg) {
        try {
            logger.info("send message =="+ msg);
            // 参数1 exchangeName
            rabbitTemplate.convertAndSend(queueName, session.getBrokerId()+"", msg );
            return true;
        } catch (Exception e) {
            logger.error("send error :" + e.getMessage());
            return false;
        }
    }

    /**
     * 包装数据，调用sendMessage
     */
    public boolean sendPack(String toId, Command command,
                            Object msg, UserSession session) {

        MessagePack pack = new MessagePack();
        pack.setCommand(command.getCommand());
        pack.setToId(toId);
        pack.setClientType(session.getClientType());
        pack.setAppId(session.getAppId());
        pack.setImei(session.getImei());
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(msg));
        pack.setData(jsonObject);

        String body = JSONObject.toJSONString(jsonObject);

        return sendMessage(session, body);
    }

    /**
     * 发送给某个用户所有端的方法
     * @param toId
     * @param command
     * @param data
     * @param appId
     * @return List<ClientInfo>  发送完消息 返回发送成功的用户信息
     */
    public List<ClientInfo> sendToUser(String toId, Command command, Object data, Integer appId){
        List<UserSession> userSession = userSessionUtils.getUserSession(appId, toId);
        List<ClientInfo> list = new ArrayList<>();
        for (UserSession session : userSession) {
            boolean b = sendPack(toId, command, data, session);
            if(b){
                list.add(new ClientInfo(session.getAppId(),session.getClientType(),session.getImei()));
            }
        }
        return list;
    }

    public void sendToUser(String toId, Integer clientType,String imei, Command command,
                           Object data, Integer appId){
        if(clientType != null && StringUtils.isNotBlank(imei)){
            ClientInfo clientInfo = new ClientInfo(appId, clientType, imei);
            sendToUserExceptClient(toId,command,data,clientInfo);
        }else{
            sendToUser(toId,command,data,appId);
        }
    }

    /**
     * 发送给某个用户指定客户端
     * @param toId
     * @param command
     * @param data
     * @param clientInfo
     */
    public void sendToUser(String toId, Command command
            , Object data, ClientInfo clientInfo){
        UserSession userSession = userSessionUtils.getUserSession(clientInfo.getAppId(), toId, clientInfo.getClientType(),
                clientInfo.getImei());
        sendPack(toId,command,data,userSession);
    }

    private boolean isMatch(UserSession sessionDto, ClientInfo clientInfo) {
        return Objects.equals(sessionDto.getAppId(), clientInfo.getAppId())
                && Objects.equals(sessionDto.getImei(), clientInfo.getImei())
                && Objects.equals(sessionDto.getClientType(), clientInfo.getClientType());
    }

    /**
     * 发送给除了某一端之外的其他端
     * @param toId
     * @param command
     * @param data
     * @param clientInfo
     */
    public void sendToUserExceptClient(String toId, Command command
            , Object data, ClientInfo clientInfo){
        List<UserSession> userSession = userSessionUtils
                .getUserSession(clientInfo.getAppId(),
                        toId);
        for (UserSession session : userSession) {
            if(!isMatch(session,clientInfo)){
                sendPack(toId,command,data,session);
            }
        }
    }

}
