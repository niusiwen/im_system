package com.nsw.im.common.enums.command;

/**
 *
 * @author nsw
 * @date 2023/11/17 14:36
 */
public enum MessageCommand implements Command {

    //单聊消息  1103
    MSG_P2P(0x44F),

    //单聊消息ack 1046
    MSG_ACK(0x416)

    ;

    private int command;

    MessageCommand(int command) {
        this.command = command;
    }

    @Override
    public int getCommand() {
        return command;
    }
}
