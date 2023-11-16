package com.nsw.im.tcp.reciver.process;

import com.nsw.im.codec.proto.MessagePack;
import com.nsw.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 默认的MQ消息处理器  --支持可拓展
 * @author nsw
 * @date 2023/11/15 21:44
 */
public abstract class BaseProcess {

    public abstract void processBefore();

    public void process(MessagePack messagePack) {
        processBefore();
        NioSocketChannel nioSocketChannel = SessionSocketHolder.get(messagePack.getAppId(),
                messagePack.getToId(), messagePack.getClientType(),
                messagePack.getImei());

        if (nioSocketChannel!= null) {
            nioSocketChannel.writeAndFlush(messagePack);
        }

        processAfter();
    }

    public abstract void processAfter();
}
