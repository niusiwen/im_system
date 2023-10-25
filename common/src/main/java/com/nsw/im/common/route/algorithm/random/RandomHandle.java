package com.nsw.im.common.route.algorithm.random;

import com.nsw.im.common.enums.UserErrorCode;
import com.nsw.im.common.exception.ApplicationException;
import com.nsw.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author nsw
 * @date 2023/10/17 19:43
 */
public class RandomHandle implements RouteHandle {

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if(size == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        int i = ThreadLocalRandom.current().nextInt(size);

        return values.get(i);
    }

}
