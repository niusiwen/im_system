package com.nsw.im.tcp.register;

import com.nsw.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;

/**
 * zookeeper注册中心
 * @author nsw
 * @date 2023/10/8 22:58
 */
public class ZKit {

    private ZkClient zkClient;

    public ZKit(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    //im-coreRoot/tcp/ip:port
    public void createRootNode(){
        boolean exists = zkClient.exists(Constants.ImCoreZkRoot);
        if(!exists){
            //创建临时节点
            zkClient.createPersistent(Constants.ImCoreZkRoot);
        }

        boolean tcpExists = zkClient.exists(Constants.ImCoreZkRoot
                +Constants.ImCoreZkRootTcp);
        if(!tcpExists){
            //创建临时节点
            zkClient.createPersistent(Constants.ImCoreZkRoot
                    +Constants.ImCoreZkRootTcp);
        }

        boolean webExists = zkClient.exists(Constants.ImCoreZkRoot
                +Constants.ImCoreZkRootWeb);
        if(!webExists){
            //创建临时节点
            zkClient.createPersistent(Constants.ImCoreZkRoot
                    +Constants.ImCoreZkRootWeb);
        }
    }

    //ip+port
    public void createNode(String path) {
        if(!zkClient.exists(path)){
            zkClient.createPersistent(path);
        }
    }

}
