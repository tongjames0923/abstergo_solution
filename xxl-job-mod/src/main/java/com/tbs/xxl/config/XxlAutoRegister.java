package com.tbs.xxl.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

/**
 * 自动注册Xxl-job
 *
 * @author abstergo
 */
public class XxlAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "Xxl Pack";
    }

    /**
     * 获取当前注册器对应的功能模块
     *
     * @return 分布式任务调度功能模块
     */
    @Override
    protected Feature getFeature() {
        return Feature.XXL_JOB;
    }

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.xxl"};
    }
}
