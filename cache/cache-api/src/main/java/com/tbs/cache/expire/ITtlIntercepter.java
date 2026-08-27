package com.tbs.cache.expire;

import com.tbs.cache.integration.AbstractTtlCache;

import java.util.Date;

/**
 * 过期拦截器
 */
public interface ITtlIntercepter {

    /**
     * 缓存过期回调
     *
     * @param cache 缓存
     * @param key   缓存key
     * @param e     过期时间
     */
    Date onTtlExpired(AbstractTtlCache cache, Object key, Date e);
}
