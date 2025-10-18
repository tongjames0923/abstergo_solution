package com.tbs.redis.config;

import com.tbs.common.util.BeanUtil;
import com.tbs.redis.codec.DefaultSystemCodec;
import com.tbs.redis.config.property.RedisProperty;
import com.tbs.redis.constants.RedisBeanNameConstants;
import org.redisson.Redisson;
import org.redisson.api.EvictionMode;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import javax.annotation.Resource;

/**
 * @author abstergo
 */
@Configuration
@ConditionalOnProperty(prefix = "tbs.redis", value = "enable", havingValue = "true")
public class BasicRedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String host;
    @Value("${spring.redis.port:6379}")
    private Integer port;
    @Value("${spring.redis.timeout:30000}")
    private long timeout;
    @Value("${spring.redis.password:123456}")
    private String password;

    @Resource
    RedisProperty property;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(
        @Qualifier(RedisBeanNameConstants.REDIS_VALUE_SERIALIZER) RedisSerializer serializer) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + this.host + ":" + this.port).setPassword(this.password);
        config.setCodec(new DefaultSystemCodec(serializer));
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

    @Bean(RedisBeanNameConstants.REDIS_VALUE_SERIALIZER)
    @ConditionalOnMissingBean(name = RedisBeanNameConstants.REDIS_VALUE_SERIALIZER)
    RedisSerializer valueSerializer() throws Throwable {
        return BeanUtil.buildBeanFromProperties(GenericJackson2JsonRedisSerializer::new, property.getValueSerializer(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean
    public RedissonSpringCacheManager redisCacheManager(RedissonClient redissonClient) {
        var cacheManager = new RedissonSpringCacheManager(redissonClient) {
            @Override
            protected CacheConfig createDefaultConfig() {
                var cacheConfig = new CacheConfig();
                cacheConfig.setEvictionMode(EvictionMode.LFU);
                cacheConfig.setMaxSize(1024);

                return cacheConfig;
            }
        };
        cacheManager.setAllowNullValues(property.isAllowNullValues());
        cacheManager.setTransactionAware(true);
        return cacheManager;
    }

}
