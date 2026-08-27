package com.tbs.schedule.config;

import com.tbs.schedule.api.ScheduleStrategy;
import com.tbs.schedule.api.TaskExecuteErrorHandle;
import com.tbs.schedule.impl.SimpleLogErrorHandle;
import com.tbs.schedule.impl.TimingWheelStrategy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * 调度器配置属性
 * <p>
 * 统一以 {@code tbs.schedule} 前缀绑定，由 {@link ScheduleConfig} 经 {@code @EnableConfigurationProperties} 激活，
 * 本类不标注 {@code @Component}（遵循中间件模块配置规范）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Getter
@Setter
@ConfigurationProperties("tbs.schedule")
public class ScheduleProperty {

    /**
     * 总开关：是否启用调度内核（默认关闭，由使用方显式开启）
     */
    private boolean enable = true;

    /**
     * 调度策略实现类，默认时间轮
     */
    private Class<? extends ScheduleStrategy> strategy = TimingWheelStrategy.class;

    /**
     * 轮询等待时间
     */
    private long pollTime = 5;

    /**
     * 轮询等待时间单位
     */
    private TimeUnit pollTimeUnit = TimeUnit.SECONDS;

    /**
     * 时间轮每个槽位的时间间隔（毫秒）
     */
    private long tickDuration = 1000;

    /**
     * 时间轮槽数量
     */
    private int wheelSize = 300;

    /**
     * 单次 poll 最大推进槽数
     */
    private int maxTicksPerPoll = 1000;

    /**
     * 定时任务执行错误处理类
     */
    private Class<? extends TaskExecuteErrorHandle> scheduleExecuteErrorHandle = SimpleLogErrorHandle.class;

    /**
     * 是否装配内置线程触发器
     * <p>
     * true（默认）：装配内置 {@code LocalThreadScheduleTrigger}，由进程内常驻线程阻塞轮询驱动内核 tick。 false：不装配内置触发器，由使用方自行注册
     * {@code ScheduleTrigger} Bean 驱动内核（例如接入任意 外部调度器时，在该调度器模块中提供调用 {@code SchedulerCore.tick()} 的触发器实现）。
     * </p>
     */
    private boolean embeddedTrigger = true;
}
