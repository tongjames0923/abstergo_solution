package com.tbs.logging.impl;

/**
 * 缓存名称常量
 * <p>
 * 集中管理本模块 Spring Caching 注解使用的 cacheNames，禁止在注解中硬编码魔法值字符串。
 * </p>
 *
 * @see com.tbs.cache.config.TbsCacheProperty.TtlConfig#getOverrides() 对应 TTL 覆盖 key 需与本类取值保持一致
 */
public final class CacheNamesConstants {

    /**
     * 所有日志操作类型缓存
     */
    public static final String ALL_OPERATE_TYPES = "logging:allOperateTypes";

    private CacheNamesConstants() {
    }
}
