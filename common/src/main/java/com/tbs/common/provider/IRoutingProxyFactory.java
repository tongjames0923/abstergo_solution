package com.tbs.common.provider;

/**
 * 路由代理工厂 SPI。
 * <p>
 * 把「注册中心」与「如何构建可在多来源间转发的代理」解耦：{@link IRegistryCenter} 只负责条目登记与解析分派，
 * 代理的创建细节（JDK 动态代理、调用转发、来源切换）由本工厂实现承接。阶段 2 仅声明契约并预留注入位，
 * 真实工厂在阶段 3 与 {@code RoutingServiceProxy} 接线时提供。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IRoutingProxyFactory {

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
    <T> T create(Class<T> interfaceType, IRegistryCenter center, RoutingStrategy strategy, String business, String version);
}
