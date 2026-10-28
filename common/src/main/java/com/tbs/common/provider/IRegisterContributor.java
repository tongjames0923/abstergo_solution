package com.tbs.common.provider;

import java.util.List;

/**
 * 注册贡献者
 *
 * @author Boolean
 */
public interface IRegisterContributor {

    /**
     * 获取注册贡献者ID
     *
     * @return 注册贡献者ID
     */
    String getContributorId();

    /**
     * 获取所有实例
     *
     * @param type     类型
     * @param business 业务线
     * @param version  版本
     * @param <T>      类型
     * @return 所有实例
     */
    <T> List<T> getAllInstanceByTypeAndBusinessAndVersion(Class<T> type, String business, String version);

    /**
     * 获取代理
     *
     * @param type
     * @param business
     * @param version
     * @param <T>
     * @param <TI>
     * @return
     */
    <T, TI> TI proxy(Class<T> type, String business, String version, Class<TI> instanceType);

}
