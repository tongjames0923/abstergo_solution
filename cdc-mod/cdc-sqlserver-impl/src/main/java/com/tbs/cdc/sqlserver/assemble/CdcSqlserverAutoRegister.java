package com.tbs.cdc.sqlserver.assemble;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

/**
 * CDC SQL Server实现自动组件注册类
 * <p>
 * 继承自{@link AbstractAutoBeanRegister}，除SQL Server实现包{@code com.tbs.cdc.sqlserver}外，
 * 同时扫描CDC通用运行时包{@code com.tbs.cdc}，使{@code @EnableCdcSqlserver}一步完成
 * 捕获实现与通用运行时的装配。重复扫描由基类按包名去重、Spring按Bean名去重，不会重复注册。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class CdcSqlserverAutoRegister extends AbstractAutoBeanRegister {

    /**
     * 获取包名称
     *
     * @return CDC SQL Server包名称
     */
    @Override
    protected String getPackName() {
        return "CDC SQL Server Pack";
    }

    /**
     * 获取当前注册器对应的功能模块
     *
     * @return CDC功能模块
     */
    @Override
    protected Feature getFeature() {
        return Feature.CDC;
    }

    /**
     * 获取基础扫描包路径
     *
     * @return 基础扫描包路径数组
     */
    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.cdc.sqlserver", "com.tbs.cdc"};
    }
}
