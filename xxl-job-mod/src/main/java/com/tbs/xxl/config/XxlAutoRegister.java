package com.tbs.xxl.config;

import com.tbs.common.AbstractAutoBeanRegister;

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

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.xxl"};
    }
}
