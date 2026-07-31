package com.tbs.shared.pojo;

/**
 * 自定义数据权限范围接口
 *
 * @author abstergo
 */
public interface ICustomDataScopeEntity {
    /**
     * 获取数据权限范围类型
     *
     * @return
     */
    String getDataScopeType();

    /**
     * 设置数据权限范围类型
     *
     * @param dateScopeType
     */
    void setDataScopeType(String dateScopeType);

    /**
     * 获取数据权限范围值
     *
     * @return
     */
    String getDataScopeValue();

    /**
     * 设置数据权限范围值
     *
     * @param dateScopeValue
     */
    void setDataScopeValue(String dateScopeValue);
}
