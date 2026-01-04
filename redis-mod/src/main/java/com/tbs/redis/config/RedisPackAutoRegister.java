package com.tbs.redis.config;

import com.tbs.common.AbstractAutoBeanRegister;

/**
 * Redis组件自动注册类
 * <p>
 * 继承自AbstractAutoBeanRegister，用于自动扫描和注册Redis相关组件
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class RedisPackAutoRegister extends AbstractAutoBeanRegister {
    /**
     * 获取包名称
     * <p>
     * 返回Redis组件包的名称，用于日志和标识
     * </p>
     *
     * @return Redis包名称
     */
    @Override
    protected String getPackName() {
        return "Redis Pack";
    }

    /**
     * 获取基础扫描包路径
     * <p>
     * 返回需要扫描的Redis相关组件的包路径数组
     * </p>
     *
     * @return 基础扫描包路径数组
     */
    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.redis"};
    }
}
