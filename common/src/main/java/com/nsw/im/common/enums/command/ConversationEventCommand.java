package com.nsw.im.common.enums.command;

/**
 * 会话
 */
public enum ConversationEventCommand implements Command {


    //删除会话
    CONVERSATION_DELETE(5000),

    //更新会话
    CONVERSATION_UPDATE(5001),


    ;

    private Integer command;

    ConversationEventCommand(int command) {
        this.command = command;
    }


    public int getCommand() {
        return command;
    }
}
