package com.tbs.common;

/**
 * 默认注册包扫描
 *
 * @author tongj
 */
public class CommonPackAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "Common Package";
    }

    @Override
    protected String[] getBasePackages() {
        return new String[] {"com.tbs.common"};
    }
}
