package com.tbs.script.config;

import com.tbs.common.AbstractAutoBeanRegister;

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

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.script"};
    }
}
