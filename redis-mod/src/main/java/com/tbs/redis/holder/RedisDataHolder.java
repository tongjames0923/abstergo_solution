package com.tbs.redis.holder;

import com.tbs.common.data.*;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.FeatureWrapperHelper;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis数据存储实现类，基于Redisson实现
 *
 * @author tongj
 */
public class RedisDataHolder implements IDataHolder, IMatchKeyFounder, IDataHolderExpireActuator {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean set(String key, Object value, boolean force) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        if (force) {
            bucket.set(value);
            return true;
        } else {
            return bucket.trySet(value);
        }
    }

    @Override
    public <T> Optional<T> get(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        T value = bucket.get();
        return Optional.ofNullable(value);
    }

    @Override
    public void remove(String key) {
        redissonClient.getBucket(key).delete();
    }

    @Override
    public void clear() {
        RKeys keys = redissonClient.getKeys();
        keys.flushall();
    }

    @Override
    public boolean isEmpty() {
        RKeys keys = redissonClient.getKeys();
        return keys.count() == 0;
    }

    @Override
    public int size() {
        RKeys keys = redissonClient.getKeys();
        return (int)keys.count();
    }

    @Override
    public boolean containsKey(String key) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.isExists();
    }

    @Override
    public Collection<Map.Entry<String, Object>> entries() {
        RKeys keys = redissonClient.getKeys();
        Iterable<String> keyIterable = keys.getKeys();
        List<Map.Entry<String, Object>> entries = new ArrayList<>();

        for (String key : keyIterable) {
            RBucket<Object> bucket = redissonClient.getBucket(key);
            Object value = bucket.get();
            if (value != null) {
                entries.add(new AbstractMap.SimpleEntry<>(key, value));
            }
        }
        return entries;
    }

    @Override
    public Collection<String> keys() {
        RKeys keys = redissonClient.getKeys();
        List<String> keyList = new ArrayList<>();
        keys.getKeys().forEach(keyList::add);
        return keyList;
    }

    @Override
    public Collection<Object> values() {
        RKeys keys = redissonClient.getKeys();
        Iterable<String> keyIterable = keys.getKeys();
        List<Object> values = new ArrayList<>();

        for (String key : keyIterable) {
            RBucket<Object> bucket = redissonClient.getBucket(key);
            Object value = bucket.get();
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    @Override
    public Collection<String> find(String key) {
        RKeys keys = redissonClient.getKeys();
        Iterable<String> matchedKeys = keys.getKeysByPattern(key);
        List<String> keyList = new ArrayList<>();
        matchedKeys.forEach(keyList::add);
        return keyList;
    }

    @Override
    public Collection<Map.Entry<String, Object>> findEntries(String reg) {
        Collection<String> keys = find(reg);
        if (CollUtil.isEmpty(keys)) {
            return Collections.emptyList();
        }
        List<Map.Entry<String, Object>> entries = keys.stream().map(key -> {
            RBucket<Object> bucket = redissonClient.getBucket(key);
            Object value = bucket.get();
            return new AbstractMap.SimpleEntry<>(key, value);
        }).collect(Collectors.toList());
        return entries;
    }

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
