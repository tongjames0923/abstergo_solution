package com.tbs.cache.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

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

    /**
     * 获取当前注册器对应的功能模块
     *
     * @return 缓存功能模块
     */
    @Override
    protected Feature getFeature() {
        return Feature.CACHE;
    }

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.cache"};
    }
}
