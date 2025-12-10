package com.tbs.redis.holder;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IMatchKeyFounder;
import com.tbs.common.util.base.CollUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Redis数据存储实现类，基于Redisson实现
 * <p>
 * 实现了IDataHolder和IMatchKeyFounder接口，提供Redis数据存储和键匹配功能
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class RedisDataHolder implements IDataHolder, IMatchKeyFounder {

    /**
     * Redisson客户端实例
     */
    @Resource
    private RedissonClient redissonClient;

    /**
     * 设置数据到Redis
     *
     * @param key   数据键
     * @param value 数据值
     * @param force 是否强制设置（true：不管键是否存在都设置，false：仅当键不存在时设置）
     * @return 是否设置成功
     */
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

    /**
     * 从Redis获取数据
     *
     * @param key 数据键
     * @param <T> 数据类型
     * @return 包含数据值的Optional，如果键不存在则返回Optional.empty()
     */
    @Override
    public <T> Optional<T> get(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        T value = bucket.get();
        return Optional.ofNullable(value);
    }

    /**
     * 从Redis删除数据
     *
     * @param key 数据键
     */
    @Override
    public void remove(String key) {
        redissonClient.getBucket(key).delete();
    }

    /**
     * 清空Redis中所有数据
     */
    @Override
    public void clear() {
        RKeys keys = redissonClient.getKeys();
        keys.flushall();
    }

    /**
     * 判断Redis是否为空
     *
     * @return true：Redis为空，false：Redis不为空
     */
    @Override
    public boolean isEmpty() {
        RKeys keys = redissonClient.getKeys();
        return keys.count() == 0;
    }

    /**
     * 获取Redis中数据的数量
     *
     * @return 数据数量
     */
    @Override
    public int size() {
        RKeys keys = redissonClient.getKeys();
        return (int)keys.count();
    }

    /**
     * 判断Redis中是否包含指定键
     *
     * @param key 数据键
     * @return true：包含指定键，false：不包含指定键
     */
    @Override
    public boolean containsKey(String key) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.isExists();
    }

    /**
     * 获取Redis中所有键值对
     *
     * @return 所有键值对的集合
     */
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

    /**
     * 获取Redis中所有键
     *
     * @return 所有键的集合
     */
    @Override
    public Collection<String> keys() {
        RKeys keys = redissonClient.getKeys();
        List<String> keyList = new ArrayList<>();
        keys.getKeys().forEach(keyList::add);
        return keyList;
    }

    /**
     * 获取Redis中所有值
     *
     * @return 所有值的集合
     */
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

    /**
     * 根据模式查找Redis中的键
     *
     * @param key 键模式（支持通配符）
     * @return 匹配的键的集合
     */
    @Override
    public Collection<String> find(String key) {
        RKeys keys = redissonClient.getKeys();
        Iterable<String> matchedKeys = keys.getKeysByPattern(key);
        List<String> keyList = new ArrayList<>();
        matchedKeys.forEach(keyList::add);
        return keyList;
    }

    /**
     * 根据模式查找Redis中的键值对
     *
     * @param reg 键模式（支持通配符）
     * @return 匹配的键值对的集合
     */
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

}
