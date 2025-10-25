package com.tbs.cache.limit.impl;

import com.tbs.cache.limit.ICacheLimiter;
import com.tbs.cache.limit.ICacheOperationRecorder;
import com.tbs.cache.manual.IManualCacheHandler;
import com.tbs.common.util.base.CollUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleCacheLimiter implements ICacheLimiter {

    private AtomicInteger count = new AtomicInteger(0);

    private static final int maxCount = 2048;

    private static final int EXECUTION_LIMIT = 128;

    @Override
    public void limitExecution(IManualCacheHandler handler, ICacheOperationRecorder recorder) {
        var cnt = count.getAndIncrement();
        if (cnt < EXECUTION_LIMIT) {
            return;
        }
        count.set(0);
        var keys = handler.keys();
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        if (keys.size() < maxCount) {
            return;
        }
        int removeCount = keys.size() - maxCount + (maxCount / 8);
        keys.stream().map((k) -> {
            return recorder.getCacheOperationView(handler, k);
        }).sorted((o1, o2) -> {
            //如果getCount为0则排在最后
            //如果getCount不为0则按照lastGetCountTime,时间越早排在越后面
            //如果getCount不为0且lastGetCountTime相同则按照putCount,putCount越小排在越前面
            return o1.getGetCount() == 0 ? 1
                : o1.getLastGetTime().compareTo(o2.getLastGetTime()) == 0 ? o1.getPutCount() - o2.getPutCount()
                    : o1.getLastGetTime().compareTo(o2.getLastGetTime());
        }).limit(removeCount).forEach((view) -> {
            handler.remove(view.getKey());
            recorder.onRemove(handler, view.getKey(), 1);
        });
        recorder.cleanUp();
    }
}
