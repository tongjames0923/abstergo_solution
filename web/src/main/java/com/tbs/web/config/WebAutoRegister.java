package com.tbs.web.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

/**
 * @author tongj
 * @description: web自动注册类
 */
public class WebAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "WEB PACK";
    }

    /**
     * 获取当前注册器对应的功能模块
     *
     * @return Web基础组件功能模块
     */
    @Override
    protected Feature getFeature() {
        return Feature.WEB;
    }

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.web"};
    }
}
