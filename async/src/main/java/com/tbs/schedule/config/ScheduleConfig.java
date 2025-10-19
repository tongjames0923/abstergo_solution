package com.tbs.schedule.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.schedule.ISchedule;
import com.tbs.schedule.IScheduleExecuteErrorHandle;
import com.tbs.schedule.impl.SimpleLogErrorHandle;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

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

}
