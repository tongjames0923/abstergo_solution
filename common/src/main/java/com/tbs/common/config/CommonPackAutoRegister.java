package com.tbs.common.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

/**
 * 通用包自动注册器
 * <p>
 * 用于自动注册com.tbs.common包下的Bean定义
 * 继承自AbstractAutoBeanRegister，实现了通用包的扫描配置
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class CommonPackAutoRegister extends AbstractAutoBeanRegister {
    /**
     * 获取注册包的名称
     *
     * @return 注册包的名称
     */
    @Override
    protected String getPackName() {
        return "Common Package";
    }

    /**
     * 获取当前注册器对应的功能模块
     *
     * @return 通用功能模块
     */
    @Override
    protected Feature getFeature() {
        return Feature.COMMON;
    }

    /**
     * 获取需要扫描的基础包路径
     *
     * @return 需要扫描的基础包路径数组
     */
    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.common"};
    }
}
