package com.nsw.im.service.user.model.req;

import com.nsw.im.common.model.RequestBase;
import com.nsw.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;

/**
 * @author nsw
 * @date 2023/8/8 22:11
 */
@Data
public class ImportUserReq extends RequestBase {

    private List<ImUserDataEntity> userData;
}
