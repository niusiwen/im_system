package com.nsw.im.service.config;

import com.nsw.im.common.config.AppConfig;
import com.nsw.im.common.enums.ImUrlRouteWayEnum;
import com.nsw.im.common.enums.RouteHashMethodEnum;
import com.nsw.im.common.route.RouteHandle;
import com.nsw.im.common.route.algorithm.consistenthash.AbstractConsistentHash;
import com.nsw.im.common.route.algorithm.consistenthash.ConsistentHashHandle;
import com.nsw.im.common.route.algorithm.consistenthash.TreeMapConsistentHash;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * @author nsw
 * @date 2023/10/17 19:45
 */
@Configuration
public class BeanConfig {

    @Autowired
    AppConfig appConfig;

    @Bean
    public ZkClient buildZKClient() {
        return new ZkClient(appConfig.getZkAddr(), appConfig.getZkConnectTimeOut());
    }

    @Bean
    public RouteHandle routeHandle() throws Exception {
//        return new RandomHandle();
//        return new LoopHandle();
//        ConsistentHashHandle consistentHashHandle = new ConsistentHashHandle();
//        TreeMapConsistentHash treeMapConsistentHash = new TreeMapConsistentHash();
//        consistentHashHandle.setHash(treeMapConsistentHash);

        Integer  imRouteWay = appConfig.getImRouteWay();
        String routeWay = "";

        ImUrlRouteWayEnum handler = ImUrlRouteWayEnum.getHandler(imRouteWay);
        routeWay = handler.getClazz();

        RouteHandle routeHandle = (RouteHandle) Class.forName(routeWay).newInstance();
        if(handler == ImUrlRouteWayEnum.HASH){

            Method setHash = Class.forName(routeWay).getMethod("setHash", AbstractConsistentHash.class);
            Integer consistentHashWay = appConfig.getConsistentHashWay();
            String hashWay = "";

            RouteHashMethodEnum hashHandler = RouteHashMethodEnum.getHandler(consistentHashWay);
            hashWay = hashHandler.getClazz();
            AbstractConsistentHash consistentHash = (AbstractConsistentHash)Class.forName(hashWay).newInstance();
            setHash.invoke(routeHandle, consistentHash);
        }

        return routeHandle;
    }
}
