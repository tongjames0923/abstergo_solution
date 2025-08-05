package com.tbs.cache.core;

import org.springframework.cache.Cache;

import java.util.Date;

/**
 * 缓存过期处理接口
 *
 * @author tongj
 */
public interface ICacheExpiredActuator extends IExpiredActuator<Cache> {

    /**
     * 缓存全部过期处理
     *
     * @param cache
     * @param expireTime
     * @return
     */
    boolean expireAll(Cache cache, Date expireTime);

}
