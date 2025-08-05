package com.tbs.cache.core.impl;

import com.tbs.cache.core.expire.AbstractSpringScheduleExpiredActuator;
import com.tbs.cache.core.expire.ICacheExpiredActuator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import java.util.Date;

/**
 * @author tongj
 */
public class LocalExpireActuatorCache extends AbstractSpringScheduleExpiredActuator<Cache>
    implements ICacheExpiredActuator {

    private static final Logger logger = LoggerFactory.getLogger(LocalExpireActuatorCache.class);

    private static class CacheRemover implements IExpireRemove<Cache> {
        @Override
        public void remove(Cache cache, Object key) {
            if (key == null) {
                cache.invalidate();
            } else {
                cache.evict(key);
            }
        }
    }

    private CacheRemover remover = new CacheRemover();

    @Override
    protected IExpireRemove<Cache> getRemoveFunction() {
        return remover;
    }

    @Override
    public boolean expireAll(Cache cache, Date expireTime) {
        return expire(cache, null, expireTime);
    }

}
