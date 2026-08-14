package com.tbs.mq.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MQ失败重试配置属性类
 * <p>
 * 通过@ConfigurationProperties注解绑定tbs.mq.retry前缀的配置属性， 集中管理重试链路的调度驱动选择与调度参数，替代零散的@Value注入。
 * 不使用@Component注册，改由MqSupportConfig经@EnableConfigurationProperties显式激活， 避免重复导入时重复注册。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Data
@ConfigurationProperties("tbs.mq.retry")
public class MqRetryProperty {

    /**
     * 内嵌调度默认调度间隔（毫秒）
     */
    public static final long DEFAULT_INTERVAL_MILLIS = 10_000L;

    /**
     * 调度驱动类型：内嵌守护线程
     */
    public static final String SCHEDULER_EMBEDDED = "embedded";

    /**
     * 调度驱动类型：外部调度平台（IBackgroundJob桥接，如xxl-job）
     */
    public static final String SCHEDULER_JOB = "job";

    /**
     * 调度驱动选择
     * <p>
     * 取值embedded或job，二选一互斥： embedded（默认）由内嵌守护线程周期触发； job注册为IBackgroundJob交由外部调度平台集中触发。
     * </p>
     */
    private String scheduler = SCHEDULER_EMBEDDED;

    /**
     * 内嵌调度间隔（毫秒）
     * <p>
     * 仅scheduler=embedded时生效，非正数时按默认间隔处理。
     * </p>
     */
    private long intervalMillis = DEFAULT_INTERVAL_MILLIS;

}
