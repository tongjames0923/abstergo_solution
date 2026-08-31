package com.tbs.schedule.impl;

import com.tbs.schedule.api.TaskExecuteErrorHandle;
import com.tbs.schedule.api.TaskSubmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的错误处理（仅打日志）
 * <p>
 * 任务失败后仅记录日志，任务即被丢弃（一次性触发语义下失败不自动重试）。业务可注册自定义
 * {@link TaskExecuteErrorHandle} 实现做告警或补偿。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SimpleLogErrorHandle implements TaskExecuteErrorHandle {

    private static final Logger log = LoggerFactory.getLogger(SimpleLogErrorHandle.class);

    @Override
    public void handle(TaskSubmission submission, Throwable throwable) {
        log.error("schedule task {} handle error", submission.getTaskId(), throwable);
    }
}
