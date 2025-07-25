package com.tbs.schedule.impl;

import com.tbs.schedule.ISchedule;
import com.tbs.schedule.IScheduleExecuteErrorHandle;
import com.tbs.schedule.IScheduleTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;

/**
 * 默认的错误处理
 * @author tongj
 */
public class SimpleLogErrorHandle implements IScheduleExecuteErrorHandle {

    private static final Logger log = LoggerFactory.getLogger(SimpleLogErrorHandle.class);

    @Override
    public void handle(IScheduleTask task, Throwable throwable, ISchedule schedule) {
        log.error(String.format("task {} handle Error . at %s", task.toString(), schedule.toString()), throwable);
    }
}
