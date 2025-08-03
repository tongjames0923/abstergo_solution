package com.tbs.cache.core.impl;

import com.tbs.cache.core.ICacheExpiredActuator;
import com.tbs.common.error.AssertUtil;
import com.tbs.schedule.ISchedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Date;

/**
 * @author tongj
 */
public class LocalExpireActuatorCache implements ICacheExpiredActuator {

    private static final Logger logger = LoggerFactory.getLogger(LocalExpireActuatorCache.class);

    @AllArgsConstructor
    @Data
    @ToString
    private static class ExpireTask implements Runnable {
        /**
         * 缓存对象
         */
        private Cache cache;
        /**
         * 缓存key
         */
        private Object key;

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

    @Override
    public boolean expire(Cache cache, Object key, Date expireTime) {
        AssertUtil.notNull(cache, "cache can not be null");
        AssertUtil.notNull(key, "key can not be null");
        AssertUtil.notNull(expireTime, "expireTime can not be null");
        long time = expireTime.getTime() - System.currentTimeMillis();
        if (time <= 0) {
            time = 1;
        }
        schedule.pin(new ExpireTask(cache, key), Duration.ofMillis(time));
        return true;
    }

    @Override
    public boolean expireAll(Cache cache, Date expireTime) {
        AssertUtil.notNull(cache, "cache can not be null");
        AssertUtil.notNull(expireTime, "expireTime can not be null");
        long time = expireTime.getTime() - System.currentTimeMillis();
        if (time <= 0) {
            time = 1;
        }
        schedule.pin(new ExpireTask(cache, null), Duration.ofMillis(time));
        return true;
    }

}
