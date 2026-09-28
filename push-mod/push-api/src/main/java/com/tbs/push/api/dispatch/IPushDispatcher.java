package com.tbs.push.api.dispatch;

import com.tbs.push.model.message.PushMessage;

/**
 * 推送消息分发器契约
 * <p>
 * 消息分发的编排入口：从消息源取消息 → 协调器解析目标 → 逐会话投递 → 记录留痕。
 * 本接口实现不持有调度线程，触发方式由外部决定（内嵌调度驱动或外部平台桥接，
 * 经配置互斥选择其一，参照mq模块的双驱动模式）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IPushDispatcher {

    /**
     * 分发单条消息
     * <p>
     * 协调器解析目标并逐会话投递，单会话失败不中断其余目标；投递结果完整留痕。
     * </p>
     *
     * @param message 待分发的业务消息
     */
    void dispatch(PushMessage message);

    /**
     * 从消息源取出一批消息并逐条分发
     */
    void dispatchDue();

    /**
     * 分发一轮消息（异常安全）
     * <p>
     * 供调度驱动方在长驻线程内调用，单轮异常仅记日志不外抛，保证调度线程存活。
     * </p>
     */
    void dispatchDueSafely();

}
