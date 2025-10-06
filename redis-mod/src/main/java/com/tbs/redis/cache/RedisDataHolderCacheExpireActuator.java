package com.tbs.redis.cache;

import com.tbs.cache.core.expire.ICacheExpiredActuator;
import com.tbs.common.error.AssertUtil;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存的过期处理，基于Redisson实现
 *
 * @author abstergo
 */
public class RedisDataHolderCacheExpireActuator implements ICacheExpiredActuator {
    private static final String KEY_VALUE_CONNECTOR = "::";
    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean expire(Cache cache, Object key, Date expireTime) {
        AssertUtil.notNull(cache, "cache can not be null");
        AssertUtil.notNull(key, "key can not be null");
        AssertUtil.notNull(expireTime, "expireTime can not be null");
        AssertUtil.hasText(cache.getName(), "cache name can not be empty");
        String keyStr = cache.getName() + KEY_VALUE_CONNECTOR + key.toString();
        long ttl = expireTime.getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            redissonClient.getBucket(keyStr).expire(ttl, TimeUnit.MILLISECONDS);
            return true;
        }
        return false;
    }

    @Override
    public boolean expireAll(Cache cache, Date expireTime) {
        AssertUtil.notNull(cache, "cache can not be null");
        AssertUtil.notNull(expireTime, "expireTime can not be null");
        AssertUtil.hasText(cache.getName(), "cache name can not be empty");
        redissonClient.getKeys().getKeysByPattern(cache.getName() + KEY_VALUE_CONNECTOR + "*").forEach(key -> {
            redissonClient.getBucket(key).expireAt(expireTime);
        });
        return true;
    }
}
