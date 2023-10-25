package com.nsw.im.codec;

import com.alibaba.fastjson.JSONObject;
import com.nsw.im.codec.proto.Message;
import com.nsw.im.codec.proto.MessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 消息解码类
 * @author nsw
 * @date 2023/9/27 22:34
 */
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext,
                          ByteBuf byteBuf, List<Object> list) throws Exception {

        // 请求头（指令
        // 版本
        // clientType
        // 消息解析类型
        // appid
        // imei长度
        // bodylen） + imei号 + body(请求体)

        if (byteBuf.readableBytes() < 20) {
            return;
        }

        /** 获取command **/
        int command = byteBuf.readInt();

        /** 获取version **/
        int version = byteBuf.readInt();

        /** 获取clientType **/
        int clientType = byteBuf.readInt();

        /** 获取messageType **/
        int messageType = byteBuf.readInt();

        /** 获取appId **/
        int appId = byteBuf.readInt();

        /** 获取imeiLength **/
        int imeiLength = byteBuf.readInt();

        /** 获取bodyLen **/
        int bodyLen = byteBuf.readInt();

        if (byteBuf.readableBytes() < bodyLen + imeiLength ) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte [] imeiData = new byte[imeiLength];
        byteBuf.readBytes(imeiData);
        String imei = new String(imeiData);

        byte [] bodyData = new byte[bodyLen];
        byteBuf.readBytes(bodyData);
        //String body = new String(bodyData);


        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setClientType(clientType);
        messageHeader.setCommand(command);
        messageHeader.setLength(bodyLen);
        messageHeader.setVersion(version);
        messageHeader.setMessageType(messageType);
        messageHeader.setImei(imei);

        Message message = new Message();
        message.setMessageHeader(messageHeader);

        if (messageType == 0x0) {
            String body = new String(bodyData);
            JSONObject parse = (JSONObject) JSONObject.parse(body);
            message.setMessagePack(parse);
        };


        byteBuf.markWriterIndex();

        list.add(message);

    }


}
