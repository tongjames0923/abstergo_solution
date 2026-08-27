package com.tbs.schedule.executor;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.schedule.api.ExecutionPolicy;
import com.tbs.schedule.api.TaskExecutor;
import com.tbs.schedule.api.TaskPayload;
import com.tbs.schedule.api.TaskSubmission;
import com.tbs.schedule.impl.LocalRunnablePayload;

/**
 * 本地 Runnable 任务执行器（默认执行器）
 * <p>
 * 处理 {@code type = local} 的任务，直接调用 {@link LocalRunnablePayload} 承载的 {@link Runnable}。
 * 作为框架兜底执行器，保证未配置其它执行器时本地任务即可运行。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class LocalRunnableTaskExecutor implements TaskExecutor {

    @Override
    public boolean supports(String taskType) {
        return LocalRunnablePayload.TYPE.equals(taskType);
    }

    @Override
    public void execute(TaskSubmission submission) {
        AssertUtil.isFalse(submission.getPayload() instanceof LocalRunnablePayload)
            .i18n("schedule.executor.local.type.mismatch").params(submission.getTaskType()).throwIt();
        ((LocalRunnablePayload) submission.getPayload()).getRunnable().run();
    }
}
