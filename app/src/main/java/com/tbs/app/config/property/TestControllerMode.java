package com.tbs.app.config.property;

/**
 * 测试控制器启用模式枚举
 * <p>
 * 用于控制公开/私有测试控制器的启用策略，
 * 配合 {@link com.tbs.app.config.condition.ConditionalOnTestControllerMode} 注解使用
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum TestControllerMode {

    /**
     * 开启公开的和私有的测试控制器
     */
    ALL,

    /**
     * 禁止公开的和私有的测试控制器
     */
    NONE,

    /**
     * 只开启公开的测试控制器
     */
    PUB,

    /**
     * 只开启私有的测试控制器
     */
    PRI
}
