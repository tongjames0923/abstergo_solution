package com.tbs.redis.holder;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IMatchKeyFounder;
import com.tbs.common.util.CollUtil;
import com.tbs.redis.config.property.RedisProperty;
import com.tbs.redis.constants.RedisBeanNameConstants;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Redis数据存储实现类
 *
 * @author tongj
 */
public class RedisDataHolder implements IDataHolder, IMatchKeyFounder {

    @Resource(name = RedisBeanNameConstants.DEFAULT_REDIS_TEMPLATE)
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisProperty redisProperty;

    @Override
    public boolean set(String key, Object value, boolean force) {
        if (force) {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } else {
            return redisTemplate.opsForValue().setIfAbsent(key, value);
        }
    }

    /**
     * 设置缓存有效期
     *
     * @param key
     * @param expireTime
     * @return
     */
    public boolean expire(String key, Date expireTime) {
        return redisTemplate.expireAt(key, expireTime);
    }

    @Override
    public <T> Optional<T> get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable((T)value);
    }

    @Override
    public void remove(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void clear() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public boolean isEmpty() {
        Set<String> keys = redisTemplate.keys("*");
        return keys == null || keys.isEmpty();
    }

    @Override
    public int size() {
        Set<String> keys = redisTemplate.keys("*");
        return keys == null ? 0 : keys.size();
    }

    @Override
    public boolean containsKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public Collection<Map.Entry<String, Object>> entries() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        List<Map.Entry<String, Object>> entries = new ArrayList<>();
        for (String key : keys) {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                entries.add(new AbstractMap.SimpleEntry<>(key, value));
            }
        }
        return entries;
    }

    @Override
    public Collection<String> keys() {
        Set<String> keys = redisTemplate.keys("*");
        return keys == null ? List.of() : keys;
    }

    @Override
    public Collection<Object> values() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        return values == null ? List.of() : values;
    }

    @Override
    public Collection<String> find(String key) {
        return redisTemplate.keys(key);
    }

    @Override
    public Collection<Map.Entry<String, Object>> findEntries(String reg) {
        Collection<String> keys = redisTemplate.keys(reg);
        if (CollUtil.isEmpty(keys)) {
            return Collections.emptyList();
        }
        List<Map.Entry<String, Object>> entries = keys.stream().map(key -> {
            Object value = redisTemplate.opsForValue().get(key);
            return new AbstractMap.SimpleEntry<>(key, value);
        }).collect(Collectors.toList());
        return entries;
    }
}
