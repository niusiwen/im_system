package com.nsw.im.tcp.reciver;

import com.alibaba.fastjson.JSONObject;
import com.nsw.im.codec.proto.MessagePack;
import com.nsw.im.common.ClientType;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.DeviceMultiLoginEnum;
import com.nsw.im.common.enums.command.SystemCommand;
import com.nsw.im.common.model.UserClientDto;
import com.nsw.im.tcp.redis.RedisManager;
import com.nsw.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 多端同步：1 单端登录 一端在线：踢掉除了本clientType + imei 以外的设备
 *          2 双端登录 允许pc/mobile 其中一端登录+web端 踢掉除了本clientType + imei 以外的web端设备
 *          3 三端登录，允许pc mobile web端登录
 * @author nsw
 * @date 2023/10/15 12:09
 */
public class UserLoginMessageListener {

    private final static Logger logger = LoggerFactory.getLogger(UserLoginMessageListener.class);

    private Integer loginModel;

    public UserLoginMessageListener(Integer loginModel) {
        this.loginModel = loginModel;
    }

    public void ListenerUserLogin() {
        RTopic topic = RedisManager.getRedissonClient().getTopic(Constants.RedisConstants.UserLoginChannel);
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String message) {
                logger.info("收到用户上线通知" + message);
                UserClientDto userClientDto = JSONObject.parseObject(message, UserClientDto.class);

                List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(userClientDto.getAppId(), userClientDto.getUserId());
                for(NioSocketChannel nioSocketChannel : nioSocketChannels){
                    if (loginModel == DeviceMultiLoginEnum.ONE.getLoginMode()) {
                        /* 单端登录 */
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

                        if(!(clientType+":"+imei).equals(userClientDto.getClientType()+":"+userClientDto.getImei())) {
                            // todo 不相等要踢掉客户端
                            /**
                             * 这里不能服务端直接将channel给close掉，因为tcp关闭连接要4次挥手，过程中还有数据在传递
                             * 如果强行将客户端给踢掉，就可能会有数据包的丢失。
                             * 所以这里的处理逻辑是告诉客户端，其他端登录，交由客户端处理，
                             * 让客户端通知服务端断开连接
                             */
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            //  把pack写回去
                            nioSocketChannel.writeAndFlush(pack);


                        }

                    } else if (loginModel == DeviceMultiLoginEnum.TWO.getLoginMode()) {
                        /* 双端登录 */
                        //web端不做处理，双端登录支持web端多端登录
                        if(userClientDto.getClientType() == ClientType.WEB.getCode()) {
                            continue;
                        }

                        // 判断当前端是否是web端，是的话也不做处理
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        if (clientType == ClientType.WEB.getCode()) {
                            continue;
                        }
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
                        if(!(clientType+":"+imei).equals(userClientDto.getClientType()+":"+userClientDto.getImei())) {
                            // todo 不相等要踢掉客户端
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            //  把pack写回去
                            nioSocketChannel.writeAndFlush(pack);

                        }


                    } else if (loginModel == DeviceMultiLoginEnum.THREE.getLoginMode()) {
                        /*三端登录*/
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
                        if(userClientDto.getClientType() == ClientType.WEB.getCode()) {
                            continue;
                        }
                        //是否是相同端
                        Boolean isSameClient = false;
                        // 如果新登录的端是手机端，并且当前端是手机端，需要处理
                        if ((clientType == ClientType.IOS.getCode() ||
                                clientType == ClientType.ANDROID.getCode()) &&
                                (clientType == ClientType.IOS.getCode() ||
                                        clientType == ClientType.ANDROID.getCode())) {
                            isSameClient = true;
                        }

                        // 如果新登录的端是pc端，并且当前端是pc端，需要处理
                        if ((clientType == ClientType.MAC.getCode() ||
                                clientType == ClientType.WINDOWS.getCode()) &&
                                (clientType == ClientType.MAC.getCode() ||
                                        clientType == ClientType.WINDOWS.getCode())) {
                            isSameClient = true;
                        }

                        if(!(clientType+":"+imei).equals(userClientDto.getClientType()+":"+userClientDto.getImei())) {
                            // todo 不相等要踢掉客户端
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            //  把pack写回去
                            nioSocketChannel.writeAndFlush(pack);
                        }


                    }

                }





            }
        });
    }

}
