package com.tbs.cache.impl;

import com.tbs.cache.integration.AbstractAdaptTtlCache;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.schedule.ISchedule;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;

import java.time.Duration;
import java.util.Date;

@Slf4j
public class AdaptScheduleExpireTtlCache extends AbstractAdaptTtlCache {
    public AdaptScheduleExpireTtlCache(Cache orginCache) {
        super(orginCache);
    }

    /**
     * 获取调度器
     *
     * @return
     */
    protected ISchedule getSchedule() {
        return SpringUtil.getBean(ISchedule.class);
    }

    @Override
    public boolean expireAll(Cache cache, Date expireTime) {
        try {
            return getSchedule().pin(() -> {
                cache.clear();
            }, Duration.between(new Date().toInstant(), expireTime.toInstant())).isPin();
        } catch (Exception e) {
            log.error("expireAll error", e);
            return false;
        }
    }

    @Override
    public boolean expire(@NotNull Cache cache, Object key, @NotNull Date expireTime) {
        try {
            return getSchedule().pin(() -> {
                cache.evict(key);
            }, Duration.between(new Date().toInstant(), expireTime.toInstant())).isPin();
        } catch (Exception e) {
            log.error("expireAll error", e);
            return false;
        }
    }
}
