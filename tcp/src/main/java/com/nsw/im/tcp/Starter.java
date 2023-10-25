package com.nsw.im.tcp;

import com.nsw.im.codec.config.BootstrapConfig;
import com.nsw.im.tcp.reciver.MessageReciver;
import com.nsw.im.tcp.redis.RedisManager;
import com.nsw.im.tcp.register.RegisterZk;
import com.nsw.im.tcp.register.ZKit;
import com.nsw.im.tcp.server.ImServer;
import com.nsw.im.tcp.server.ImWebSocketServer;
import com.nsw.im.tcp.utils.MqFactory;
import org.I0Itec.zkclient.ZkClient;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author nsw
 * @date 2023/8/9 23:27
 */
public class Starter {


    // 自定义私有协议
    // client IOS 安卓 pc(windows mac) //支持json 也支持 protobuf
    //appid
    //28 + imei + body
    //请求头（指令 版本 clientType 消息解析类型 imei长度 appid bodylen） + imei号 + body(请求体)


    public static void main(String[] args) {
        if (args.length > 0) {
            start(args[0]);
        }
    }


    private static void start(String path) {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(path);
            BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);

            new ImServer(bootstrapConfig.getNim()).start();
            new ImWebSocketServer(bootstrapConfig.getNim()).start();

            RedisManager.init(bootstrapConfig);

            MqFactory.init(bootstrapConfig.getNim().getRabbitmq());

            MessageReciver.init(bootstrapConfig.getNim().getBrokerId() + "");

            registerZK(bootstrapConfig);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(500);
        }
    }

    public static void registerZK(BootstrapConfig config) throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        ZkClient zkClient = new ZkClient(config.getNim().getZkConfig().getZkAddr(),
                config.getNim().getZkConfig().getZkConnectTimeOut());
        ZKit zKit = new ZKit(zkClient);
        RegisterZk registerZk = new RegisterZk(zKit, hostAddress, config.getNim());
        Thread thread = new Thread(registerZk);
        thread.start();
    }
}
