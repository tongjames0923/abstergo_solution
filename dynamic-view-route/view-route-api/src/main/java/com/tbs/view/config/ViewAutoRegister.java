package com.tbs.view.config;

import com.tbs.common.AbstractAutoBeanRegister;

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

    @Override
    protected String[] getBasePackages() {
        return new String[] {"com.tbs.view"};
    }
}
