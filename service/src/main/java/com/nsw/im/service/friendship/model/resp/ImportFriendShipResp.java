package com.nsw.im.service.friendship.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @author nsw
 * @date 2023/8/12 10:06
 */
@Data
public class ImportFriendShipResp {

    private List<String> successId;

    private List<String> errorId;
}
