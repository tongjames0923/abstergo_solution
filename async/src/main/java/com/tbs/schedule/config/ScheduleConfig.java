package com.tbs.schedule.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.schedule.AbstractTimingWheelAsyncBackgroundSchedule;
import com.tbs.schedule.ISchedule;
import com.tbs.schedule.IScheduleExecuteErrorHandle;
import com.tbs.schedule.impl.SimpleLogErrorHandle;
import com.tbs.schedule.impl.TimingWheelSchedule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务配置
 *
 * @author tongj
 */
@Configuration
public class ScheduleConfig {

    @Resource
    private ScheduleProperty scheduleProperty;

    @Bean
    @ConditionalOnClass(ISchedule.class)
    @ConditionalOnMissingBean(IScheduleExecuteErrorHandle.class)
    public IScheduleExecuteErrorHandle scheduleExecuteErrorHandle() {
        return BeanUtil.buildBeanFromProperties(SimpleLogErrorHandle::new,
            scheduleProperty.getScheduleExecuteErrorHandle(), BeanUtil::buildByEmptyArgs);
    }

    /**
     * 时间轮调度器配置
     * <p>
     * 配置时间轮调度器，用于处理定时任务
     * </p>
     *
     * @return 时间轮调度器实例
     */
    @Bean
    @ConditionalOnMissingBean(AbstractTimingWheelAsyncBackgroundSchedule.class)
    @ConditionalOnClass(IScheduleExecuteErrorHandle.class)
    AbstractTimingWheelAsyncBackgroundSchedule timingWheelSchedule(IScheduleExecuteErrorHandle errorHandle) {
        return new TimingWheelSchedule(errorHandle, 5L, TimeUnit.SECONDS, 1L, 60 * 5, TimeUnit.SECONDS);
    }

}
