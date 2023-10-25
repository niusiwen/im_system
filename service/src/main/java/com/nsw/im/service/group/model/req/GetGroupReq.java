package com.nsw.im.service.group.model.req;

import com.nsw.im.common.model.RequestBase;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class GetGroupReq extends RequestBase {

    private String groupId;

}
