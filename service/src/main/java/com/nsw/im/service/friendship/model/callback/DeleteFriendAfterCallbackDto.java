package com.nsw.im.service.friendship.model.callback;

import com.nsw.im.service.friendship.model.req.FriendDto;
import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class DeleteFriendAfterCallbackDto {

    private String fromId;

    private String toId;
}
