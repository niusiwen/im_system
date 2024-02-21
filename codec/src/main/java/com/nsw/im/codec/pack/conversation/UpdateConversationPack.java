package com.nsw.im.codec.pack.conversation;

import lombok.Data;

/**
 * @author nsw
 * @date 2024/2/20 17:37
 */
@Data
public class UpdateConversationPack {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private Integer conversationType;

    private Long sequence;
}
