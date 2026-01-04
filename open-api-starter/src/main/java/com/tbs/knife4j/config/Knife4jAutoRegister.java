package com.tbs.knife4j.config;

import com.tbs.common.AbstractAutoBeanRegister;

/**
 * knife4j自动注册
 *
 * @author tongj
 */
public class Knife4jAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "Knife4j Package";
    }

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.knife4j"};
    }
}
