package com.nsw.im.service.friendship.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 好友分组成员表
 */
@Data
@TableName("im_friendship_group_member")
public class ImFriendShipGroupMemberEntity {

    @TableId(value = "group_id")
    private Long groupId;

    private String toId;

}
