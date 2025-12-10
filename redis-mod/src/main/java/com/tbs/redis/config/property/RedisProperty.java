package com.tbs.redis.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

/**
 * Redis配置属性类
 * <p>
 * 通过@ConfigurationProperties注解绑定tbs.redis前缀的配置属性
 * 用于配置Redis相关功能的开关和序列化器
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@ConfigurationProperties("tbs.redis")
@Component
public class RedisProperty {

    /**
     * 是否启用Redis功能
     * <p>
     * 控制是否加载和初始化Redis相关组件
     * </p>
     */
    private boolean enable = false;

    /**
     * Redis值的序列化器类
     * <p>
     * 指定用于Redis值序列化的实现类
     * 默认使用JdkSerializationRedisSerializer
     * </p>
     */
    private Class<? extends RedisSerializer> ValueSerializer = JdkSerializationRedisSerializer.class;

}
