package com.tbs.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 推送消息全链路视图
 * <p>
 * 单消息全链路审计拼图：SEND 行为（日志表）+ 投递关系（接收人表）+ 处理行为（日志表）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushMessageChainVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息本体信息
     */
    private PushNotifyMessageVO message;

    /**
     * 发送行为事件（日志表 SEND 记录，缺失时为null）
     */
    private OperationVO sendEvent;

    /**
     * 接收人ID列表（投递关系）
     */
    private List<Long> receivers;

    /**
     * 处理行为事件列表（READ/CLICK等，不含SEND）
     */
    private List<OperationVO> operations;

    /**
     * 操作行为事件视图
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OperationVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 操作人（系统/服务发起时为null）
         */
        private Long operator;

        /**
         * 操作类型
         */
        private String opType;

        /**
         * 来源通道（http/rpc/job/system）
         */
        private String opSource;

        /**
         * 操作时间
         */
        private Date opTime;

        /**
         * 操作上下文扩展
         */
        private String extInfo;

    }

}
