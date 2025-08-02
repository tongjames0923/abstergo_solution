package com.tbs.redis.cache;

import com.tbs.cache.core.IExpiredActuator;
import com.tbs.common.error.AssertUtil;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * Redis缓存过期处理
 *
 * @author tongj
 */
public class RedisCacheExpiredActuator implements IExpiredActuator {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean expire(Cache cache, Object key, long expireTime) {
        // 通过RedisTemplate为指定键设置过期时间
        // 将毫秒转换为秒，因为Redis的expire方法通常使用秒作为单位
        String cacheKey = cache.getName() + "::" + key.toString();
        return redisTemplate.expire(cacheKey, expireTime, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Deprecated
    @Override
    public boolean expireAll(Cache cache, long expireTime) {
        // 使用scan命令查找所有匹配的键并为它们设置过期时间
        String pattern = cache.getName() + "::" + "*";

        // 创建ScanOptions配置，每次扫描1000个键
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();

        // 使用scan命令查找所有匹配的键
        Set<String> keys = new HashSet<>();
        try {
            Set<String> finalKeys = keys;
            redisTemplate.execute((RedisCallback<Object>)connection -> {
                connection.scan(options).forEachRemaining(key -> {
                    finalKeys.add(new String(key));
                });
                return null;
            });
        } catch (Exception e) {
            // 如果scan执行失败，回退到使用keys方法
            keys = redisTemplate.keys(pattern);
        }

        // 为所有找到的键设置过期时间
        boolean allSuccess = true;
        for (String key : keys) {
            if (!redisTemplate.expire(key, expireTime, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }
}
