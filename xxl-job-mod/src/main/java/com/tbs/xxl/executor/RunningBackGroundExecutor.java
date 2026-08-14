package com.tbs.xxl.executor;

import com.tbs.common.delegate.IBackgroundJob;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

/**
 * 后台任务执行器，用于执行所有IBackgroundJob类型的bean
 * <p>
 * 该执行器通过XXL-JOB调度框架触发，从Spring容器中获取所有实现了IBackgroundJob接口的bean，
 * 并依次执行它们。执行过程中会记录详细的日志信息。
 * </p>
 * @author abstergo
 */
public class RunningBackGroundExecutor {

    /**
     * XXL-JOB任务处理程序，执行所有后台任务
     * <p>
     * 从Spring容器中获取所有IBackgroundJob类型的bean，并依次执行。
     * 如果没有找到任何后台任务，则记录日志并返回。
     * 执行过程中捕获异常并记录错误日志。
     * </p>
     */
    @XxlJob("runningBackGroundJobHandler")
    public void runningBackGroundJobHandler() {
        /* 获取所有IBackgroundJob类型的bean */
        var jobs = SpringUtil.getBeansByTypeSafe(IBackgroundJob.class);
        /* 检查是否有后台任务需要执行 */
        if (CollUtil.isEmpty(jobs)) {
            XxlJobHelper.log("No background jobs found.");
            return;
        }
        /* 遍历并执行所有后台任务 */
        for (var job : jobs) {
            XxlJobHelper.log("Executing background job: {} description: {}", job.name(), job.description());
            /* 执行后台任务，捕获异常并记录错误 */
            try {
                job.execute();
            } catch (Exception e) {
                XxlJobHelper.log("job {} error!", job.name());
                XxlJobHelper.log(e);
            }
            XxlJobHelper.log("Executing background job: {} completed.", job.name());
        }
    }
}
