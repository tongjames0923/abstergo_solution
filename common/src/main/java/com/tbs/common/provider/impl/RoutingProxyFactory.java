package com.tbs.common.provider.impl;

import java.lang.reflect.Proxy;

import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.IRoutingProxyFactory;
import com.tbs.common.provider.RoutingStrategy;
import com.tbs.common.provider.impl.support.RoutingServiceProxy;

/**
 * 默认路由代理工厂。
 * <p>
 * {@link IRoutingProxyFactory} 的实现：以 JDK 动态代理为目标接口创建 {@link RoutingServiceProxy} 调用处理器，
 * 处理器绑定注册中心与 business/version 定向维度，每次方法调用经 {@code center.resolveFirst} 择一路由目标后转发。
 * 工厂无状态，代理的复用缓存在 {@link IRegistryCenter} 侧按 (接口, 策略, business, version) 承接。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class RoutingProxyFactory implements IRoutingProxyFactory {

    /**
     * 构建绑定到指定 business/version 的路由代理
     *
     * @param interfaceType 目标接口类型，必须为接口，不能为null
     * @param center        解析所依赖的注册中心，不能为null
     * @param strategy      来源偏好，不能为null
     * @param business      绑定业务，null/空表示不约束
     * @param version       绑定版本，null/空表示不约束
     * @param <T>           目标接口类型
     * @return 路由代理实例
     */
    @Override
    public <T> T create(Class<T> interfaceType, IRegistryCenter center, RoutingStrategy strategy, String business,
        String version) {
        return interfaceType.cast(Proxy.newProxyInstance(interfaceType.getClassLoader(),
            new Class<?>[] {interfaceType},
            new RoutingServiceProxy<>(interfaceType, center, strategy, business, version)));
    }
}
