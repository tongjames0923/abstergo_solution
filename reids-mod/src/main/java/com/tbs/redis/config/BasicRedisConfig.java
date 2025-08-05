package com.tbs.redis.config;

import com.tbs.common.util.BeanUtil;
import com.tbs.redis.config.property.RedisProperty;
import com.tbs.redis.constants.RedisBeanNameConstants;
import com.tbs.redis.lock.RedisLockProvider;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.ErrorHandler;

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
    @ConditionalOnBean(RedisLockProvider.class)
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + this.host + ":" + this.port).setPassword(this.password);
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
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
    RedisTemplate redisTemplate(RedisConnectionFactory connectionFactory,
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
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(RedisCacheConfiguration.class)
    RedisCacheConfiguration redisConfiguration(RedisProperty property) {
        var redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
        if (property.getCacheTimeout() > 0) {
            redisCacheConfiguration =
                redisCacheConfiguration.entryTtl(java.time.Duration.ofMillis(property.getCacheTimeout()));
        }
        if (!property.isAllowNullValues()) {
            return redisCacheConfiguration.disableCachingNullValues();
        }
        return redisCacheConfiguration;
    }

    @Bean
    @ConditionalOnMissingBean(RedisCacheWriter.class)
    RedisCacheWriter writer(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheWriter.lockingRedisCacheWriter(redisConnectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean(RedisCacheManager.class)
    public RedisCacheManager redisCacheManager(RedisCacheWriter redisCacheWriter,
        RedisCacheConfiguration redisCacheConfiguration) {
        return RedisCacheManager.builder().cacheWriter(redisCacheWriter).cacheDefaults(redisCacheConfiguration)
            .enableStatistics().build();
    }

    private static final Logger REDIS_MESSAGE_CENTER_LOGGER =
        LoggerFactory.getLogger(RedisMessageListenerContainer.class);

    @Bean
    @ConditionalOnMissingBean(RedisMessageListenerContainer.class)
    RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.setErrorHandler(new ErrorHandler() {

            @Override
            public void handleError(Throwable t) {
                REDIS_MESSAGE_CENTER_LOGGER.error("redis message listener error", t);
            }
        });
        return container;
    }

}
