package com.tbs.cache.limit;

import com.tbs.cache.manual.IManualCacheHandler;

/**
 * 缓存执行限制器
 */
public interface ICacheLimiter {

    /**
     * 缓存执行限制
     */
    void limitExecution(IManualCacheHandler handler, ICacheOperationRecorder recorder);
}
