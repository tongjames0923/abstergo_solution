package com.tbs.web.config;

import com.tbs.common.AbstractAutoBeanRegister;

/**
 * @author tongj
 * @description: web自动注册类
 */
public class WebAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "WEB PACK";
    }

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.web"};
    }
}
