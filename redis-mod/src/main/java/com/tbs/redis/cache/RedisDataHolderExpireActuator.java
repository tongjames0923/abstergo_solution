package com.tbs.redis.cache;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderExpireActuator;
import com.tbs.common.data.IPartitionableDataHolder;
import com.tbs.common.data.ValueWrapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.FeatureWrapperHelper;
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
        ValueWrapper<String> keyWrapper = ValueWrapper.empty();
        Object finalKey = key;
        FeatureWrapperHelper.unwrap(cache, IPartitionableDataHolder.class).ifPresent((partitionable) -> {
            keyWrapper.set(partitionable.reKey(finalKey.toString()));
        });
        String strKey = keyWrapper.getOrThrow().toString();
        ValueWrapper<Boolean> result = ValueWrapper.of(false);
        FeatureWrapperHelper.unwrap(cache, RedisDataHolder.class).ifPresentOrElse(redisDataHolder -> {
            RBucket<Object> bucket = redissonClient.getBucket(strKey.toString());
            long ttl = expireTime.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                bucket.expire(ttl, TimeUnit.MILLISECONDS);
                result.set(true);
                return;
            }
        }, () -> {
            AssertUtil.throwError(() -> new UnsupportedOperationException("cache is not RedisDataHolder"));
        });
        return result.getOrThrow();
    }

}
