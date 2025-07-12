package com.tbs.cache.core.impl;

import com.tbs.cache.core.IExpiredActuator;
import com.tbs.common.util.AssertUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;

import java.util.Date;
import java.util.concurrent.*;

/**
 * @author tongj
 */
public class LocalExpireActuator implements IExpiredActuator, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(LocalExpireActuator.class);

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    private boolean isInit = false;

    @AllArgsConstructor
    @Data
    private static class ExpireTask implements Delayed {
        /**
         * 过期时间长度
         */
        private long expireTime;
        /**
         * 缓存对象
         */
        private Cache cache;
        /**
         * 缓存key
         */
        private Object key;
        /**
         * 创建时间
         */
        private Date createDate;

        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return unit.convert(expireTime + createDate.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(@NotNull Delayed o) {
            return (int)(this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public String toString() {
            return "ExpireTask{" +
                "expireTime=" +
                expireTime +
                ", cache=" +
                cache +
                ", key=" +
                key +
                ", createDate=" +
                createDate +
                '}';
        }
    }

    private DelayQueue<ExpireTask> delayQueue = new DelayQueue<>();

    CacheManager cacheManager;

    @Override
    @Async
    public void init() {
        while (isInit) {
            try {
                ExpireTask task = delayQueue.poll();
                if (task != null) {
                    logger.trace("开始执行过期任务... {}", task);
                    Cache cache = task.getCache();
                    if (cache == null) {
                        logger.warn("缓存不存在，请检查缓存配置是否正确！");
                        continue;
                    }
                    if (task.getKey() == null) {
                        cache.invalidate();
                        logger.trace("执行缓存清除任务...{}", task);
                    } else {
                        cache.evict(task.getKey());
                        logger.trace("执行缓存清理任务...{}", task);
                    }
                }
            } catch (Exception e) {
                logger.error("ExpireTask error", e);
            }

        }
    }

    @Override
    public boolean expire(Cache cache, Object key, long expireTime) {
        AssertUtil.notNull(cache, "cache can not be null");
        AssertUtil.notNull(key, "key can not be null");
        delayQueue.offer(new ExpireTask(expireTime, cache, key, new Date()));
        return true;
    }

    @Override
    public boolean expireAll(Cache cache, long expireTime) {
        AssertUtil.notNull(cache, "cache can not be null");
        delayQueue.offer(new ExpireTask(expireTime, cache, null, new Date()));
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("start expire actuator");
        isInit = true;
        executorService.execute(() -> {
            init();
        });
        logger.info("expire actuator init finished");
    }

    @Override
    public void destroy() throws Exception {
        isInit = false;
    }
}
