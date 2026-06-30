package com.tbs.schedule.config;

import com.tbs.schedule.IScheduleExecuteErrorHandle;
import com.tbs.schedule.ScheduleStrategy;
import com.tbs.schedule.impl.SimpleLogErrorHandle;
import com.tbs.schedule.impl.TimingWheelStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 调度器配置属性
 *
 * @author abstergo
 */
@Data
@ConfigurationProperties("tbs.schedule")
@Component
public class ScheduleProperty {

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
    private Class<? extends IScheduleExecuteErrorHandle> scheduleExecuteErrorHandle = SimpleLogErrorHandle.class;
}
