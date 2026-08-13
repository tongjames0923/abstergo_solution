package com.tbs.mq.model;

/**
 * 消息分派结果
 * <p>
 * 描述一条消息经分派编排后的终态，供消费容器据此决定ACK后的后续动作
 * （无需额外动作/持久化待重试/死信留痕等）。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public enum DispatchOutcome {

    /**
     * 处理成功
     */
    SUCCESS,

    /**
     * 幂等拦截：重复消息，跳过处理
     */
    DUPLICATE_SKIPPED,

    /**
     * 可重试失败：消息应被持久化到失败存储，等待延迟重投递
     */
    RETRY_SCHEDULED,

    /**
     * 业务性失败：重试无意义，按配置丢弃或死信留痕
     */
    BUSINESS_DISCARDED,

    /**
     * 死信：跳过重试，直接进入死信队列留痕
     */
    DEAD_LETTER

}
