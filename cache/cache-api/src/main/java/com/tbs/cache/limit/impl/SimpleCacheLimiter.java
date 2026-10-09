package com.tbs.cache.limit.impl;

import com.tbs.cache.limit.ICacheLimiter;
import com.tbs.cache.limit.ICacheOperationRecorder;
import com.tbs.cache.manual.IManualCacheHandler;
import com.tbs.common.util.base.CollUtil;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleCacheLimiter implements ICacheLimiter {

    private AtomicInteger count = new AtomicInteger(0);

    /**
     * 淘汰并发护栏：读/写路径已不再持有互斥锁，同一时刻仅允许一个线程执行全量淘汰，
     * 其余线程直接跳过，避免多个请求线程同时跑 O(N log N) 形成惊群
     */
    private final AtomicBoolean evicting = new AtomicBoolean(false);

    private static final int maxCount = 2048;

    private static final int EXECUTION_LIMIT = 128;

    @Override
    public void limitExecution(IManualCacheHandler handler, ICacheOperationRecorder recorder) {
        var cnt = count.getAndIncrement();
        if (cnt < EXECUTION_LIMIT) {
            return;
        }
        count.set(0);
        // 抢占淘汰执行权，抢不到说明已有线程在淘汰，本次直接跳过
        if (!evicting.compareAndSet(false, true)) {
            return;
        }
        try {
            evict(handler, recorder);
        } finally {
            evicting.set(false);
        }
    }

    private void evict(IManualCacheHandler handler, ICacheOperationRecorder recorder) {
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
        }).filter(Objects::nonNull).sorted((o1, o2) -> {
            // 如果getCount为0则排在最后（最不容易被淘汰）
            boolean g1Zero = o1.getGetCount() == 0;
            boolean g2Zero = o2.getGetCount() == 0;
            if (g1Zero && g2Zero) {
                return Integer.compare(o1.getPutCount(), o2.getPutCount());
            }
            if (g1Zero) {
                return 1;
            }
            if (g2Zero) {
                return -1;
            }
            // 如果getCount不为0则按照lastGetTime,时间越早排在越前面
            // lastGetTime为null视为"从未被get"，排后面
            Date t1 = o1.getLastGetTime();
            Date t2 = o2.getLastGetTime();
            if (t1 == null && t2 == null) {
                return Integer.compare(o1.getPutCount(), o2.getPutCount());
            }
            if (t1 == null) {
                return 1;
            }
            if (t2 == null) {
                return -1;
            }
            int c = t1.compareTo(t2);
            // 如果getCount不为0且lastGetTime相同则按照putCount,putCount越小排在越前面
            if (c == 0) {
                return Integer.compare(o1.getPutCount(), o2.getPutCount());
            }
            return c;
        }).limit(removeCount).forEach((view) -> {
            handler.remove(view.getKey());
            recorder.onRemove(handler, view.getKey(), 1);
        });
        recorder.cleanUp();
    }
}
