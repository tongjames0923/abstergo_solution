package com.tbs.cache.core;

import org.springframework.cache.Cache;

/**
 * 缓存过期处理接口
 *
 * @author tongj
 */
public interface IExpiredActuator {
    /**
     * 缓存过期处理
     *
     * @param cache
     * @param key
     * @param expireTime
     * @return
     */
    boolean expire(Cache cache, Object key, long expireTime);

    /**
     * 缓存全部过期处理
     *
     * @param cache
     * @param expireTime
     * @return
     */
    boolean expireAll(Cache cache, long expireTime);
    
}
