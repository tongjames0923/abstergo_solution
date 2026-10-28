package com.tbs.common.provider;

/**
 * 路由元信息接口
 */
public interface IRouteMeta {
    /**
     * 获取业务线
     */
    String business();

    /**
     * 获取版本号
     */
    String version();

    /**
     * 获取类型全限定名
     */
    String getTypeFullName();
}
