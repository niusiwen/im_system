package com.nsw.im.common.route.algorithm.consistenthash;

import com.nsw.im.common.route.RouteHandle;

import java.util.List;

/**
 * 一直性哈希
 * @author nsw
 * @date 2023/10/17 22:49
 */
public class ConsistentHashHandle implements RouteHandle {

    //实现一致性哈希有很多种方式，可以用TreeMap，也可以底层自己实现
    private AbstractConsistentHash hash;

    public void setHash(AbstractConsistentHash hash){
        this.hash = hash;
    }

    @Override
    public String routeServer(List <String> values, String key) {
        return hash.process(values, key);
    }
}
