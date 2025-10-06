package com.tbs.redis.cache;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderExpireActuator;
import com.tbs.common.data.IDataHolderFeatureWrapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.redis.holder.RedisDataHolder;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * RedisDataHolderExpireActuator，基于Redisson实现
 *
 * @author tongj
 */
public class RedisDataHolderExpireActuator implements IDataHolderExpireActuator {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean expire(@NotNull IDataHolder cache, Object key, @NotNull Date expireTime) {
        if (cache instanceof IDataHolderFeatureWrapper) {
            IDataHolderFeatureWrapper wrapper = (IDataHolderFeatureWrapper)cache;
            cache = wrapper.getOrginal();
            key = wrapper.reKey(key.toString());
        }
        if (cache instanceof RedisDataHolder) {
            RBucket<Object> bucket = redissonClient.getBucket(key.toString());
            long ttl = expireTime.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                bucket.expire(ttl, TimeUnit.MILLISECONDS);
                return true;
            }
            return false;
        }
        AssertUtil.throwError(() -> new UnsupportedOperationException("cache is not RedisDataHolder"));
        return false;
    }

}