package com.tbs.cdc.mysql.assemble;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

/**
 * CDC MySQL实现自动组件注册类
 * <p>
 * 继承自{@link AbstractAutoBeanRegister}，除MySQL实现包{@code com.tbs.cdc.mysql}外，
 * 同时扫描CDC通用运行时包{@code com.tbs.cdc}，使{@code @EnableCdcMysql}一步完成
 * 捕获实现与通用运行时的装配。重复扫描由基类按包名去重、Spring按Bean名去重，不会重复注册。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class CdcMysqlAutoRegister extends AbstractAutoBeanRegister {

    /**
     * 获取包名称
     *
     * @return CDC MySQL包名称
     */
    @Override
    protected String getPackName() {
        return "CDC MySQL Pack";
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
        return new String[] {"com.tbs.cdc.mysql", "com.tbs.cdc"};
    }
}
