package com.tbs.shared.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

/**
 * @author tongj
 */
public class SharedAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "Shared Lib Package";
    }

    /**
     * 获取当前注册器对应的功能模块
     *
     * @return 共享业务库功能模块
     */
    @Override
    protected Feature getFeature() {
        return Feature.SHARED;
    }

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.shared"};
    }
}
