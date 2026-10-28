package com.tbs.common.provider;

/**
 * 路由元信息接口
 */
public interface IRouteMeta {
    /**
     * 获取业务线
     */
    default String business() {
        return "";
    }

    /**
     * 获取版本号
     */
    default String version() {
        return "";
    }

    /**
     * 获取类型全限定名
     */
    default String getTypeFullName() {
        return "";
    }
}
