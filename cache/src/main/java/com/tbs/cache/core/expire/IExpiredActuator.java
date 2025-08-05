package com.tbs.cache.core.expire;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * 通用过期处理接口
 *
 * @author tongj
 */
public interface IExpiredActuator<RAW> {
    /**
     * 缓存过期处理
     *
     * @param cache
     * @param key
     * @param expireTime
     * @return
     */
    boolean expire(@NotNull RAW cache, Object key, @NotNull Date expireTime);
}
