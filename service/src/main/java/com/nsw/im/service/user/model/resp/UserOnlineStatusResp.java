package com.nsw.im.service.user.model.resp;

// import com.nsw.im.common.model.UserSession;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class UserOnlineStatusResp {

    // private List<UserSession> session;

    private String customText;

    private Integer customStatus;

}
