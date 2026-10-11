package com.tbs.redis.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.redis.codec.DefaultSystemCodec;
import com.tbs.redis.config.property.RedisProperty;
import com.tbs.redis.constants.RedisBeanNameConstants;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import jakarta.annotation.Resource;

/**
 * Redis基础配置类
 * <p>
 * 提供Redis相关基础组件的配置，包括Redisson客户端和Redis序列化器
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(RedisProperty.class)
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

    @Bean
    RedissonAutoConfigurationCustomizer redissonAutoConfigurationCustomizer() {
        return new RedissonAutoConfigurationCustomizer() {
            @Override
            public void customize(Config configuration) {
                //                configuration.useSingleServer().setAddress("redis://" + this.host + ":" + this.port)
                //                    .setPassword(this.password);
                var codec = SpringUtil.getBeanOptional(Codec.class).orElse(new DefaultSystemCodec(
                    SpringUtil.getBean(RedisBeanNameConstants.REDIS_VALUE_SERIALIZER, RedisSerializer.class)));
                configuration.setCodec(codec);
                configuration.setThreads(8);
                configuration.setNettyThreads(8);
            }
        };
    }

    //
    //    /**
    //     * 创建Redisson客户端实例
    //     * <p>
    //     * 配置Redisson客户端，使用单服务器模式连接Redis，设置密码和编解码器
    //     * 仅当tbs.redis.enable属性为true且不存在RedissonClient实例时创建
    //     * </p>
    //     *
    //     * @param serializer Redis值序列化器
    //     * @return Redisson客户端实例
    //     */
    //    @Bean(destroyMethod = "shutdown")
    //    @ConditionalOnMissingBean(RedissonClient.class)
    //    @ConditionalOnProperty(prefix = "tbs.redis", value = "enable", havingValue = "true")
    //    public RedissonClient redissonClient(
    //        @Qualifier(RedisBeanNameConstants.REDIS_VALUE_SERIALIZER) RedisSerializer serializer) {
    //        Config config = new Config();
    //        config.useSingleServer().setAddress("redis://" + this.host + ":" + this.port).setPassword(this.password);
    //        var codec= SpringUtil.getBeanOptional(Codec.class).orElse(new DefaultSystemCodec(
    //            SpringUtil.getBean(RedisBeanNameConstants.REDIS_VALUE_SERIALIZER, RedisSerializer.class)));
    //        config.setCodec(codec);
    //        RedissonClient redissonClient = Redisson.create(config);
    //        return redissonClient;
    //    }

    /**
     * 创建Redis值序列化器
     * <p>
     * 使用BeanUtil从配置属性构建GenericJackson2JsonRedisSerializer实例
     * 仅当tbs.redis.enable属性为true且不存在REDIS_VALUE_SERIALIZER实例时创建
     * </p>
     *
     * @return Redis值序列化器
     * @throws Throwable 构建过程中可能出现的异常
     */
    @Bean(RedisBeanNameConstants.REDIS_VALUE_SERIALIZER)
    @ConditionalOnMissingBean(name = RedisBeanNameConstants.REDIS_VALUE_SERIALIZER)
    @ConditionalOnProperty(prefix = "tbs.redis", value = "enable", havingValue = "true", matchIfMissing = true)
    RedisSerializer valueSerializer() throws Throwable {
        return BeanUtil.buildBeanFromProperties(() -> new GenericJackson2JsonRedisSerializer(),
            property.getValueSerializer(),
            BeanUtil::buildByEmptyArgs);
    }

}
