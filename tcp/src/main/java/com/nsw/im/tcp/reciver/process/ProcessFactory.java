package com.nsw.im.tcp.reciver.process;

/**
 * mq消息处理器的工厂类
 * @author nsw
 * @date 2023/11/15 21:43
 */
public class ProcessFactory {

    private static BaseProcess defaultProcess;

    static {
        defaultProcess = new BaseProcess() {
            @Override
            public void processBefore() {

            }

            @Override
            public void processAfter() {

            }
        };
    }

    public static BaseProcess getMessageProcess(Integer command) {

        // 这里可以拓展其他的
        return defaultProcess;
    }

}
