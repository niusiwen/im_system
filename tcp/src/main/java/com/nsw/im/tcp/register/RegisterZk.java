package com.nsw.im.tcp.register;

import com.nsw.im.codec.config.BootstrapConfig;
import com.nsw.im.common.constant.Constants;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nsw
 * @date 2023/10/8 23:14
 */
@Slf4j
public class RegisterZk implements Runnable {

    private ZKit zKit;

    private String ip;

    private BootstrapConfig.TcpConfig tcpConfig;

    public RegisterZk(ZKit zKit, String ip, BootstrapConfig.TcpConfig tcpConfig) {
        this.zKit = zKit;
        this.ip = ip;
        this.tcpConfig = tcpConfig;
    }

    @Override
    public void run() {

        zKit.createRootNode();
        String tcpPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp + "/" + ip + ":" + tcpConfig.getTcpPort();
        zKit.createNode(tcpPath);
        log.info("Register zookeeper tcpPath success, msg=[{}]", tcpPath);

        String webPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb + "/" + ip + ":" + tcpConfig.getWebSocketPort();
        zKit.createNode(webPath);
        log.info("Register zookeeper webPath success, msg=[{}]", webPath);
    }
}
