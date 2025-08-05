package com.tbs.redis.constants;

/**
 * RedisBeanNameConstants 类用于定义 Redis 相关的 Bean 名称常量
 * 这些常量在 Spring 等框架中配置和使用 Redis 时非常有用
 * 通过使用这些常量，可以确保 Bean 名称的一致性和避免拼写错误
 * @author tongj
 */
public class RedisBeanNameConstants {
    /**
     * 默认的 RedisTemplate Bean 名称
     * RedisTemplate 是用于操作 Redis 的模板类，包含了一系列操作 Redis 的方法
     */
    public static final String DEFAULT_REDIS_TEMPLATE = "DEFAULT_REDIS_TEMPLATE";

    /**
     * Redis Key 序列化器 Bean 名称
     * Key 序列化器用于将对象转换为 Redis 存储的字节流格式
     * 这对于确保 Key 的正确存储和检索至关重要
     */
    public static final String REDIS_KEY_SERIALIZER="REDIS_KEY_SERIALIZER";

    /**
     * Redis Value 序列化器 Bean 名称
     * Value 序列化器负责将对象转换为 Redis 存储的字节流格式
     * 这对于确保 Value 的正确存储和检索至关重要
     */
    public static final String REDIS_VALUE_SERIALIZER="REDIS_VALUE_SERIALIZER";

    /**
     * Redis Hash Key 序列化器 Bean 名称
     * Hash Key 序列化器用于将对象转换为 Redis Hash 结构中 Key 的字节流格式
     * 这对于确保 Hash 结构中 Key 的正确存储和检索至关重要
     */
    public static final String REDIS_HASH_KEY_SERIALIZER="REDIS_HASH_KEY_SERIALIZER";

    /**
     * Redis Hash Value 序列化器 Bean 名称
     * Hash Value 序列化器用于将对象转换为 Redis Hash 结构中 Value 的字节流格式
     * 这对于确保 Hash 结构中 Value 的正确存储和检索至关重要
     */
    public static final String REDIS_HASH_VALUE_SERIALIZER="REDIS_HASH_VALUE_SERIALIZER";
}
