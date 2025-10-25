package com.tbs.cache.limit;

import com.tbs.cache.manual.IManualCacheHandler;

public interface ICacheOperationRecorder {

    /**
     * 缓存存入
     *
     * @param key
     */
    void onPut(IManualCacheHandler handler, String key, int count);

    /**
     * 缓存获取
     *
     * @param key
     */
    void onGet(IManualCacheHandler handler, String key, int count);

    /**
     * 缓存删除
     *
     * @param key
     */
    void onRemove(IManualCacheHandler handler, String key, int count);

    /**
     * 获取缓存操作信息
     *
     * @param key
     * @return
     */
    ICacheOperationView getCacheOperationView(IManualCacheHandler handler, String key);

    /**
     * 清理缓存操作信息
     */
    void cleanUp();
}
