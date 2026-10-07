package com.tbs.common.provider;

/**
 * 路由策略枚举
 * <p>
 * 定义路由代理在 Spring 来源与 Dubbo 来源之间选择实现实例的策略。
 * 策略在每次方法调用时动态决策，支持运行时来源切换：
 * 默认 {@link #LOCAL_FIRST} 与项目「服务提供方本地实现 + @DubboService 同时导出」的部署形态契合——
 * 服务提供方进程内命中本地实现，纯消费方进程自动路由到 Dubbo。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum RoutingStrategy {

    /**
     * 本地优先：Spring 来源优先，无本地实现时降级到 Dubbo 来源（默认策略）
     */
    LOCAL_FIRST,

    /**
     * 远程优先：Dubbo 来源优先，无远程实例时降级到 Spring 来源
     */
    REMOTE_FIRST,

    /**
     * 仅本地：只使用 Spring 来源，Dubbo 来源不可见
     */
    SPRING_ONLY,

    /**
     * 仅远程：只使用 Dubbo 来源，Spring 来源不可见
     */
    DUBBO_ONLY;

    /**
     * 判断策略是否接受指定来源的实例
     *
     * @param source 待判定的对象来源，不能为null
     * @return true 表示该来源可纳入路由选择
     */
    public boolean accept(ObjectSource source) {
        if (source == ObjectSource.SPRING) {
            return this != DUBBO_ONLY;
        }
        return this != SPRING_ONLY;
    }

    /**
     * 获取策略的首选来源
     *
     * @return 首选对象来源
     */
    public ObjectSource primarySource() {
        return springFirst() ? ObjectSource.SPRING : ObjectSource.DUBBO;
    }

    /**
     * 获取策略的首选未命中时的降级来源
     *
     * @return 降级对象来源
     */
    public ObjectSource backupSource() {
        return springFirst() ? ObjectSource.DUBBO : ObjectSource.SPRING;
    }

    /**
     * 判断策略是否为 Spring 来源优先
     *
     * @return true 表示 Spring 优先于 Dubbo
     */
    private boolean springFirst() {
        return this == LOCAL_FIRST || this == SPRING_ONLY;
    }
}
