package com.tbs.cache.config;

import com.tbs.common.AbstractAutoBeanRegister;

/**
 * 缓存自动注册
 *
 * @author tongj
 */
public class CachePackAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "Cache Package";
    }

    @Override
    protected String[] getBasePackages() {
        return new String[] {"com.tbs.cache"};
    }
}
