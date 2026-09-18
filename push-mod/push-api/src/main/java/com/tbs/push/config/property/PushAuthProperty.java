package com.tbs.push.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 推送连接认证配置属性类
 * <p>
 * 通过@ConfigurationProperties注解绑定tbs.push.auth前缀的配置属性，
 * 集中管理连接凭证的签发参数，替代零散的@Value注入。
 * 不使用@Component注册，改由PushSupportConfig经@EnableConfigurationProperties显式激活。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@ConfigurationProperties("tbs.push.auth")
public class PushAuthProperty {

    /**
     * 默认凭证有效期（毫秒）
     */
    public static final long DEFAULT_TICKET_TTL_MILLIS = 60_000L;

    /**
     * 默认内存凭证存储容量
     */
    public static final int DEFAULT_CREDENTIAL_CAPACITY = 10_000;

    /**
     * 凭证有效期（毫秒）
     * <p>
     * 一次性连接凭证自签发起的有效时长，过期消费失败需重新认证；非正数按默认处理。
     * </p>
     */
    private long ticketTtlMillis = DEFAULT_TICKET_TTL_MILLIS;

    /**
     * 内存凭证存储容量上限
     * <p>
     * 仅默认兜底的内存凭证存储生效，容量满时优先惰性清理过期凭证，仍满则拒绝签发。
     * </p>
     */
    private int credentialCapacity = DEFAULT_CREDENTIAL_CAPACITY;

}
