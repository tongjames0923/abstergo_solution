package com.tbs.redis.cache;

import com.tbs.cache.core.expire.ICacheExpiredActuator;
import com.tbs.common.data.IFeatureWrapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.redis.constants.RedisBeanNameConstants;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Redis缓存的过期处理
 *
 * @author abstergo
 */
public class RedisCacheExpireActuator implements ICacheExpiredActuator {

    @Resource(name = RedisBeanNameConstants.DEFAULT_REDIS_TEMPLATE)
    private RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_VALUE_CONNECTOR = "::";


    @Override
    public boolean expire(Cache cache, Object key, Date expireTime) {
        if (cache instanceof IFeatureWrapper) {
            cache = (Cache)((IFeatureWrapper)cache).getOrginal();
        }
        AssertUtil.isTrue(!(cache instanceof RedisCache),
            "cache is not RedisCache,RedisCacheExpireActuator only support RedisCache");
        return redisTemplate.expireAt(cache.getName() + KEY_VALUE_CONNECTOR + key, expireTime);
    }

    @Override
    public boolean expireAll(Cache cache, Date expireTime) {
        return redisTemplate.expireAt(cache.getName() + KEY_VALUE_CONNECTOR + "*", expireTime);
    }
}
