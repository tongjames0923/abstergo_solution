package com.tbs.cdc.assemble;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

/**
 * CDC通用运行时自动组件注册类
 * <p>
 * 继承自{@link AbstractAutoBeanRegister}，扫描{@code com.tbs.cdc}包下的配置与运行时组件，
 * 并将{@link Feature#CDC}标记为已注册。重复导入时由基类按包名去重，不会重复注册。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class CdcAutoRegister extends AbstractAutoBeanRegister {

    /**
     * 获取包名称
     *
     * @return CDC包名称
     */
    @Override
    protected String getPackName() {
        return "CDC Pack";
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
        return new String[] {"com.tbs.cdc"};
    }
}
