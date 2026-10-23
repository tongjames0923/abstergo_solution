package com.tbs.common.provider;

/**
 * 没有提供者处理接口
 */
public interface INoProviderHandler {
    /**
     * 处理没有提供者的情况
     *
     * @param service 服务名
     * @param method  方法名
     */
    void handle(Class<?> clazz, String method, Object[] args);
}
