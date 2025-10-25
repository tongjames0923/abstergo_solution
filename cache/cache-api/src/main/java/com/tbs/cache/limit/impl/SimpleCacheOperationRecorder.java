package com.tbs.cache.limit.impl;

import com.tbs.cache.limit.ICacheOperationRecorder;
import com.tbs.cache.limit.ICacheOperationView;
import com.tbs.cache.manual.IManualCacheHandler;
import lombok.Data;
import org.springframework.scheduling.annotation.Async;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存操作记录器
 */
public class SimpleCacheOperationRecorder implements ICacheOperationRecorder {
    @Data

    public class SimpleCacheOperationView implements ICacheOperationView {
        private String key;
        private int putCount;
        private int getCount;
        private int removeCount;
        private Date lastPutTime;
        private Date lastGetTime;
        private Date lastRemoveTime;

        public SimpleCacheOperationView(String key) {
            this.key = key;
        }
    }

    private String makeKey(IManualCacheHandler handler, String key) {
        return handler.toString() + ":" + key;
    }

    private ConcurrentHashMap<String, SimpleCacheOperationView> cacheOperationMap = new ConcurrentHashMap<>();

    @Async
    @Override
    public void onPut(IManualCacheHandler handler, String key, int count) {
        var k = makeKey(handler, key);
        var cacheOperationView = cacheOperationMap.computeIfAbsent(k, k1 -> new SimpleCacheOperationView(k1));
        if (count > 0) {
            cacheOperationView.setPutCount(cacheOperationView.getPutCount() + count);
            cacheOperationView.setLastPutTime(new Date());
            cacheOperationMap.put(k, cacheOperationView);
        }
    }

    @Async
    @Override
    public void onGet(IManualCacheHandler handler, String key, int count) {
        var k = makeKey(handler, key);
        var cacheOperationView = cacheOperationMap.computeIfAbsent(k, k1 -> new SimpleCacheOperationView(k1));
        if (count > 0) {
            cacheOperationView.setGetCount(cacheOperationView.getGetCount() + count);
            cacheOperationView.setLastGetTime(new Date());
            cacheOperationMap.put(k, cacheOperationView);
        }
    }

    @Async
    @Override
    public void onRemove(IManualCacheHandler handler, String key, int count) {
        var k = makeKey(handler, key);
        var cacheOperationView = cacheOperationMap.computeIfAbsent(k, k1 -> new SimpleCacheOperationView(k1));
        if (count > 0) {
            cacheOperationView.setRemoveCount(cacheOperationView.getRemoveCount() + count);
            cacheOperationView.setLastRemoveTime(new Date());
            cacheOperationMap.put(k, cacheOperationView);
        }
    }

    @Override
    public ICacheOperationView getCacheOperationView(IManualCacheHandler handler, String key) {
        return cacheOperationMap.get(makeKey(handler, key));
    }

    @Override
    public void cleanUp() {
        cacheOperationMap.clear();
    }
}
