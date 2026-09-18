package com.tbs.push.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 推送基础配置属性类
 * <p>
 * 通过@ConfigurationProperties注解绑定tbs.push前缀的配置属性，
 * 集中管理心跳与内存兜底容量参数，替代零散的@Value注入。
 * 不使用@Component注册，改由PushSupportConfig经@EnableConfigurationProperties显式激活，
 * 避免重复导入时重复注册。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@ConfigurationProperties("tbs.push")
public class PushProperty {

    /**
     * 默认心跳空闲超时（毫秒）
     */
    public static final long DEFAULT_HEARTBEAT_TIMEOUT_MILLIS = 90_000L;

    /**
     * 默认内存消息源容量
     */
    public static final int DEFAULT_MEMORY_MESSAGE_CAPACITY = 10_000;

    /**
     * 默认内存投递记录容量
     */
    public static final int DEFAULT_MEMORY_RECORD_CAPACITY = 10_000;

    /**
     * 心跳空闲超时（毫秒）
     * <p>
     * 会话最近活性距今超过该值即判定为死连接并关闭注销；
     * 非正数表示关闭心跳监控。
     * </p>
     */
    private long heartbeatTimeoutMillis = DEFAULT_HEARTBEAT_TIMEOUT_MILLIS;

    /**
     * 内存消息源容量上限
     * <p>
     * 仅默认兜底的内存消息源生效，容量满时淘汰最早入源的消息。
     * </p>
     */
    private int memoryMessageCapacity = DEFAULT_MEMORY_MESSAGE_CAPACITY;

    /**
     * 内存投递记录容量上限
     * <p>
     * 仅默认兜底的内存投递记录存储生效，容量满时淘汰最早的记录。
     * </p>
     */
    private int memoryRecordCapacity = DEFAULT_MEMORY_RECORD_CAPACITY;

}
