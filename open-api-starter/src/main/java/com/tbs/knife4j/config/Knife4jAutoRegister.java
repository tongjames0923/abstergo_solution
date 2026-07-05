package com.tbs.knife4j.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

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

    /**
     * 获取当前注册器对应的功能模块
     *
     * @return API文档功能模块
     */
    @Override
    protected Feature getFeature() {
        return Feature.KNIFE4J;
    }

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.knife4j"};
    }
}
