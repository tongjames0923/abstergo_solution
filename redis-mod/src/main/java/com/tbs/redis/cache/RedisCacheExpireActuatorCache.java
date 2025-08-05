package com.tbs.redis.cache;

import com.tbs.cache.core.expire.ICacheExpiredActuator;
import com.tbs.redis.constants.RedisBeanNameConstants;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Redis缓存的过期处理
 *
 * @author abstergo
 */
public class RedisCacheExpireActuatorCache implements ICacheExpiredActuator {

    @Resource(name = RedisBeanNameConstants.DEFAULT_REDIS_TEMPLATE)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean expire(Cache cache, Object key, Date expireTime) {
        boolean ok = redisTemplate.expireAt(cache.getName() + "::" + key, expireTime);
        return ok;
    }

    @Override
    public boolean expireAll(Cache cache, Date expireTime) {
        return redisTemplate.expireAt(cache.getName() + "::*", expireTime);
    }
}
