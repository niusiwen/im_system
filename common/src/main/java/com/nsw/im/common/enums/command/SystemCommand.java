package com.nsw.im.common.enums.command;

public enum SystemCommand implements Command {

    /**
     * 心跳检测 9999 十六进制 0x270f
     */
    PING(0x270f),

    /**
     * 登录 9000 十六进制：0x2328
     */
    LOGIN(0x2328),

    /**
     * 登出 9003 十六进制 0X232b
     */
    LOGOUT(0X232b),

    /**
     * 下线通知 用于多端互斥  9002
     */
    MUTUALLOGIN(0x232a),
    ;

    private int command;

    SystemCommand(int command){
        this.command = command;
    }


    @Override
    public int getCommand() {
        return command;
    }
}
