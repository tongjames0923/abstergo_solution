package com.tbs.view.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

/**
 * 视图自动注册类
 *
 * @author tongj
 */
public class ViewAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "View Package";
    }

    /**
     * 获取当前注册器对应的功能模块
     *
     * @return 动态视图路由功能模块
     */
    @Override
    protected Feature getFeature() {
        return Feature.VIEW_ROUTE;
    }

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.view"};
    }
}
