package com.tbs.common.provider;

public interface INoProviderHandler {
    /**
     * 无提供者处理
     *
     * @param business 业务线
     * @param ver      版本号
     * @param targetInterface 目标接口
     */
    void handle(String business, String ver, Class<?> targetInterface);
}
