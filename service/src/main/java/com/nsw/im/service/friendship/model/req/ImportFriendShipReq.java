package com.nsw.im.service.friendship.model.req;

import com.nsw.im.common.enums.FriendShipStatusEnum;
import com.nsw.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author nsw
 * @date 2023/8/12 9:52
 */
@Data
public class ImportFriendShipReq extends RequestBase {

    /**
     * 添加人
     */
    @NotBlank(message = "fromId不能为空")
    private String fromId;

    /**
     * 好友信息
     */
    private List<ImportFriendDto> friendItem;

    @Data
    public static class ImportFriendDto{

        /**
         * 好友id
         */
        private String toId;

        /**
         * 黑名单
         */
        private String remark;

        /**
         * 添加来源
         */
        private String addSource;

        /**
         * 好友状态
         */
        private Integer status = FriendShipStatusEnum.FRIEND_STATUS_NO_FRIEND.getCode();

        /**
         * 黑名单
         */
        private Integer black = FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode();
    }

}
