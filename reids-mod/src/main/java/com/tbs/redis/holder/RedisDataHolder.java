package com.tbs.redis.holder;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IMatchKeyFounder;
import com.tbs.common.util.CollUtil;
import com.tbs.redis.config.property.RedisProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.AbstractMap;

/**
 * Redis数据存储实现类
 *
 * @author tongj
 */
@Component
public class RedisDataHolder implements IDataHolder, IMatchKeyFounder {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisProperty redisProperty;

    @Override
    public boolean set(String key, Object value, boolean force) {
        if (force) {
            redisTemplate.opsForValue()
                .set(key, value, redisProperty.getCacheTimeout(), java.util.concurrent.TimeUnit.SECONDS);
            return true;
        } else {
            return redisTemplate.opsForValue()
                .setIfAbsent(key, value, redisProperty.getCacheTimeout(), java.util.concurrent.TimeUnit.SECONDS);
        }
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
        // 使用Lua脚本实现原子性操作，一次性获取所有匹配键的键值对
        String script = "local keys = redis.call('keys', ARGV[1])\n" +
            "local result = {}\n" +
            "for i=1, #keys do\n" +
            "    local value = redis.call('get', keys[i])\n" +
            "    if value ~= false then\n" +
            "        table.insert(result, keys[i])\n" +
            "        table.insert(result, value)\n" +
            "    end\n" +
            "end\n" +
            "return result";

        RedisScript<List> redisScript = new DefaultRedisScript<>(script, List.class);
        List<Object> result = redisTemplate.execute(redisScript, Collections.emptyList(), reg);
        if (CollUtil.isEmpty(result)) {
            return List.of();
        }
        List<Map.Entry<String, Object>> entries = new ArrayList<>();
        for (int i = 0; i < result.size(); i += 2) {
            String key = (String)result.get(i);
            Object value = result.get(i + 1);
            entries.add(new AbstractMap.SimpleEntry<>(key, value));
        }

        return entries;
    }
}
