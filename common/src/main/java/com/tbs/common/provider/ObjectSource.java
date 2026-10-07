package com.tbs.common.provider;

/**
 * 对象实例来源枚举
 * <p>
 * 标识一个被解析出的对象实例来自哪个能力来源，供多实现枚举、路由选择与来源区分使用。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum ObjectSource {

    /**
     * Spring 容器来源：容器内普通 Bean 或经 @DubboReference 注册进容器的引用 Bean
     */
    SPRING,

    /**
     * Dubbo 来源：经注册中心发现的服务提供者实例
     */
    DUBBO;

    /**
     * 判断当前来源是否为 Dubbo 来源
     *
     * @return true 表示 Dubbo 来源
     */
    public boolean isDubbo() {
        return this == DUBBO;
    }

    /**
     * 判断当前来源是否为 Spring 来源
     *
     * @return true 表示 Spring 来源
     */
    public boolean isSpring() {
        return this == SPRING;
    }
}
