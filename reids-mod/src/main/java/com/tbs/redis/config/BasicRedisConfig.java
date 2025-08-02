package com.tbs.redis.config;

import com.tbs.common.util.BeanUtil;
import com.tbs.redis.constants.RedisBeanNameConstants;
import com.tbs.redis.config.property.RedisProperty;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;
import java.time.Duration;

/**
 * @author abstergo
 */
@Configuration
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
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + this.host + ":" + this.port).setPassword(this.password);
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

    @Bean
    StringRedisSerializer stringRedisSerializer() {

        return new StringRedisSerializer();
    }

    @Bean(RedisBeanNameConstants.REDIS_KEY_SERIALIZER)
    @ConditionalOnMissingBean(name = RedisBeanNameConstants.REDIS_KEY_SERIALIZER)
    RedisSerializer keySerializer() throws Throwable {
        return BeanUtil.buildBeanFromProperties(StringRedisSerializer::new, property.getKeySerializer(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean(RedisBeanNameConstants.REDIS_VALUE_SERIALIZER)
    @ConditionalOnMissingBean(name = RedisBeanNameConstants.REDIS_VALUE_SERIALIZER)
    RedisSerializer valueSerializer() throws Throwable {
        return BeanUtil.buildBeanFromProperties(GenericJackson2JsonRedisSerializer::new, property.getValueSerializer(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean(RedisBeanNameConstants.REDIS_HASH_KEY_SERIALIZER)
    @ConditionalOnMissingBean(name = RedisBeanNameConstants.REDIS_HASH_KEY_SERIALIZER)
    RedisSerializer hashKeySerializer() throws Throwable {
        return BeanUtil.buildBeanFromProperties(StringRedisSerializer::new, property.getHashKeySerializer(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean(RedisBeanNameConstants.REDIS_HASH_VALUE_SERIALIZER)
    @ConditionalOnMissingBean(name = RedisBeanNameConstants.REDIS_HASH_VALUE_SERIALIZER)
    RedisSerializer hashValueSerializer() throws Throwable {
        return BeanUtil.buildBeanFromProperties(GenericJackson2JsonRedisSerializer::new,
            property.getHashValueSerializer(), BeanUtil::buildByEmptyArgs);
    }

    @Bean(RedisBeanNameConstants.DEFAULT_REDIS_TEMPLATE)
    @Primary
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
        @Qualifier(RedisBeanNameConstants.REDIS_KEY_SERIALIZER) RedisSerializer keySerializer,
        @Qualifier(RedisBeanNameConstants.REDIS_VALUE_SERIALIZER) RedisSerializer valueSerializer,
        @Qualifier(RedisBeanNameConstants.REDIS_HASH_KEY_SERIALIZER) RedisSerializer hashKeySerializer,
        @Qualifier(RedisBeanNameConstants.REDIS_HASH_VALUE_SERIALIZER) RedisSerializer hashValueSerializer) {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(hashKeySerializer);
        template.setHashValueSerializer(hashValueSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(RedisCacheConfiguration.class)
    RedisCacheConfiguration redisConfiguration(RedisProperty property) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();

        property.setAllowNullValues(property.isAllowNullValues());

        redisCacheConfiguration.entryTtl(Duration.ofMillis(property.getCacheTimeout()));
        return redisCacheConfiguration;
    }

    @Bean
    @ConditionalOnMissingBean(RedisCacheManager.class)
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory,
        RedisCacheConfiguration redisCacheConfiguration, RedisProperty property) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.lockingRedisCacheWriter(redisConnectionFactory);
        RedisCacheManager redisCacheManager = new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);
        return redisCacheManager;
    }

}
