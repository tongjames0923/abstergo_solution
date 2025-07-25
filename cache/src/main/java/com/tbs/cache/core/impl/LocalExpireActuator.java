package com.tbs.cache.core.impl;

import com.tbs.cache.core.IExpiredActuator;
import com.tbs.common.error.AssertUtil;
import com.tbs.schedule.ISchedule;
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

import javax.annotation.Resource;
import java.time.Duration;
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
    private static class ExpireTask implements Runnable {
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
        public void run() {
            logger.trace("开始执行过期任务... {}", this);
            Cache cache = this.getCache();
            if (cache == null) {
                logger.warn("缓存不存在，请检查缓存配置是否正确！");
                return;
            }
            if (this.getKey() == null) {
                cache.invalidate();
                logger.trace("执行缓存清除任务...{}", this);
            } else {
                cache.evict(this.getKey());
                logger.trace("执行缓存清理任务...{}", this);
            }
        }
    }

    @Resource
    private ISchedule schedule;

    CacheManager cacheManager;

    @Override
    public boolean expire(Cache cache, Object key, long expireTime) {
        AssertUtil.notNull(cache, "cache can not be null");
        AssertUtil.notNull(key, "key can not be null");
        schedule.pin(new ExpireTask(expireTime, cache, key, new Date()), Duration.ofMillis(expireTime));
        return true;
    }

    @Override
    public boolean expireAll(Cache cache, long expireTime) {
        AssertUtil.notNull(cache, "cache can not be null");
        schedule.pin(new ExpireTask(expireTime, cache, null, new Date()), Duration.ofMillis(expireTime));
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
