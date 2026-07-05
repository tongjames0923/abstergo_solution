package com.tbs.script.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

/**
 * 自动注册脚本引擎
 *
 * @author tongj
 */
public class AutoScriptRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "Script Pack";
    }

    /**
     * 获取当前注册器对应的功能模块
     *
     * @return 脚本引擎功能模块
     */
    @Override
    protected Feature getFeature() {
        return Feature.SCRIPT;
    }

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.script"};
    }
}
