package com.tbs.common.provider;

/**
 * 路由协调器
 */
public interface IRouteCoordinator {

    /**
     * 根据业务线和版本号路由到对应的实现类
     *
     * @param targetClass    目标类
     * @param interfaceClass 接口类
     * @param business       业务线
     * @param version        版本号
     * @return 是否路由成功
     */
    boolean route(Class<?> targetClass, Class<?> interfaceClass, String business, String version);
}
