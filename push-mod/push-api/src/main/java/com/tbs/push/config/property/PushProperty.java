package com.tbs.push.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
     * 默认公开可订阅主题白名单
     */
    public static final List<String> DEFAULT_GRANT_PUBLIC_TOPICS = Collections.unmodifiableList(
        Collections.singletonList("global.broadcast"));

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

    /**
     * 公开可订阅主题白名单
     * <p>
     * 授权集的配置层来源：内置授权器将这些主题模式（支持订阅侧通配符）
     * 无条件授予全部认证会话，与本人专属频道等硬编码层合并为授权集。
     * </p>
     */
    private List<String> grantPublicTopics = new ArrayList<>(DEFAULT_GRANT_PUBLIC_TOPICS);

}
