package com.tbs.redis.config;

import com.tbs.common.AbstractAutoBeanRegister;

/**
 * 自动注册Redis Pack
 */
public class RedisPackAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "Redis Pack";
    }

    @Override
    protected String[] getBasePackages() {
        return new String[] {"com.tbs.redis"};
    }
}
