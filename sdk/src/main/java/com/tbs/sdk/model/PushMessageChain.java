package com.tbs.sdk.model;

import java.util.List;
import lombok.Data;

/**
 * 消息全链路审计视图
 * <p>
 * 对应服务端PushMessageChainVO：SEND事件 + 投递关系 + 处理行为拼图。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushMessageChain {

    /**
     * 消息本体视图
     */
    private PushNotifyMessage message;

    /**
     * 发送事件
     */
    private Operation sendEvent;

    /**
     * 投递目标用户ID列表
     */
    private List<Long> receivers;

    /**
     * 处理行为列表（READ/CLICK等）
     */
    private List<Operation> operations;

    /**
     * 操作行为视图
     * <p>
     * 对应服务端OperationVO内部结构。
     * </p>
     */
    @Data
    public static class Operation {

        /**
         * 操作人用户ID
         */
        private Long operator;

        /**
         * 操作类型（READ/CLICK等）
         */
        private String opType;

        /**
         * 操作来源（http/rpc/job/system）
         */
        private String opSource;

        /**
         * 操作时间
         */
        private java.util.Date opTime;

        /**
         * 扩展信息
         */
        private String extInfo;
    }
}
