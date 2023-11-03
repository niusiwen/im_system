package com.nsw.im.service.friendship.model.callback;

import com.nsw.im.service.friendship.model.req.FriendDto;
import lombok.Data;

/**
 * @author nsw
 * @date 2023/11/2 23:07
 */
@Data
public class AddFriendAfterCallbackDto {

    private String fromId;

    private FriendDto toItem;
}
