package com.tbs.common.registry;

/**
 * 分派策略枚举。
 * <p>
 * 描述一次解析请求是取首个命中还是取全部命中：FIRST 对应路由代理的单实例转发，
 * ALL 对应多实现广播（如消息消费、事件监听）。与「来源偏好」{@code RoutingStrategy} 正交——
 * 本枚举决定取几个，来源偏好决定按什么顺序取。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum DispatchPolicy {

    /**
     * 择一：按顺序取第一个命中即返回
     */
    FIRST,

    /**
     * 广播：返回全部命中，顺序由来源偏好与条目优先级决定
     */
    ALL
}
