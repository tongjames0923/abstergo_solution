package com.tbs.schedule.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.schedule.*;
import com.tbs.schedule.impl.SimpleLogErrorHandle;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务配置
 *
 * @author abstergo
 */
@Configuration
public class ScheduleConfig {

    @Resource
    private ScheduleProperty scheduleProperty;

    @Bean
    @ConditionalOnMissingBean(IScheduleExecuteErrorHandle.class)
    public IScheduleExecuteErrorHandle scheduleExecuteErrorHandle() {
        return BeanUtil.buildBeanFromProperties(SimpleLogErrorHandle::new,
            scheduleProperty.getScheduleExecuteErrorHandle(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(ISchedule.class)
    @ConditionalOnClass(IScheduleExecuteErrorHandle.class)
    public DefaultSchedule defaultSchedule(IScheduleExecuteErrorHandle errorHandle,
        ScheduleStrategy<IScheduleTask> strategy) {
        TimeUnit unit = scheduleProperty.getPollTimeUnit();
        return new DefaultSchedule(errorHandle, scheduleProperty.getPollTime(), unit, strategy);
    }

    @Bean
    @ConditionalOnMissingBean(ScheduleStrategy.class)
    public ScheduleStrategy<IScheduleTask> scheduleStrategy() {
        return buildStrategy();
    }

    private ScheduleStrategy<IScheduleTask> buildStrategy() {
        Class<? extends ScheduleStrategy> clazz = scheduleProperty.getStrategy();
        try {
            // 优先尝试带配置参数的构造函数（适用于 TimingWheelStrategy 等）
            return clazz.getConstructor(long.class, int.class, int.class)
                .newInstance(scheduleProperty.getTickDuration(), scheduleProperty.getWheelSize(),
                    scheduleProperty.getMaxTicksPerPoll());
        } catch (NoSuchMethodException e) {
            try {
                // 无参构造函数（适用于 DelayQueueStrategy 等）
                return clazz.getConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("无法实例化调度策略: " + clazz.getName(), ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("无法实例化调度策略: " + clazz.getName(), e);
        }
    }
}
