package com.tbs.common.provider.impl;

import org.springframework.beans.factory.FactoryBean;

import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.RoutingStrategy;

import lombok.Getter;

/**
 * 多来源路由服务工厂 Bean
 * <p>
 * 将多来源路由代理注册为指定接口类型的 Spring Bean：getObject 返回经 {@link IRegistryCenter}
 * 构建的 JDK 动态路由代理，业务侧通过 @Resource/@Autowired 注入该接口时，使用方式与普通 Bean 完全一致，
 * 调用时由代理按路由策略在 Spring Bean 与 Dubbo 实例间动态选择目标。
 * </p>
 *
 * @param <T> 目标接口类型
 * @author abstergo
 * @since 1.0.0
 * @see RoutingServiceProxy 路由代理调用处理器
 */
public class RoutingServiceFactoryBean<T> implements FactoryBean<T> {

    /**
     * 目标接口类型
     */
    @Getter
    private final Class<T> interfaceType;

    /**
     * 路由策略
     */
    @Getter
    private final RoutingStrategy strategy;

    /**
     * 统一注册中心
     */
    private final IRegistryCenter registryCenter;

    /**
     * 构造路由服务工厂 Bean
     *
     * @param interfaceType  目标接口类型，不能为null
     * @param strategy       路由策略，不能为null
     * @param registryCenter 统一注册中心，不能为null
     */
    public RoutingServiceFactoryBean(Class<T> interfaceType, RoutingStrategy strategy, IRegistryCenter registryCenter) {
        this.interfaceType = interfaceType;
        this.strategy = strategy;
        this.registryCenter = registryCenter;
    }

    /**
     * 返回路由代理实例
     *
     * @return 多来源路由代理
     */
    @Override
    public T getObject() {
        return registryCenter.getRoutingProxy(interfaceType, strategy, null, null);
    }

    /**
     * 返回代理暴露的对象类型
     *
     * @return 目标接口类型
     */
    @Override
    public Class<?> getObjectType() {
        return interfaceType;
    }

    /**
     * 代理为单例：同一接口同一策略在提供器内部缓存复用
     *
     * @return true 表示单例
     */
    @Override
    public boolean isSingleton() {
        return true;
    }
}
