package com.tbs.schedule.config;

import com.tbs.schedule.IScheduleExecuteErrorHandle;
import com.tbs.schedule.impl.SimpleLogErrorHandle;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("tbs.schedule")
public class ScheduleProperty {
    /**
     * 定时任务执行错误处理
     */
    private Class<? extends IScheduleExecuteErrorHandle> scheduleExecuteErrorHandle = SimpleLogErrorHandle.class;
}
