package com.tbs.xxl.executor;

import com.tbs.common.delegate.IBackgroundJob;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 *
 * @author abstergo
 */
@Component
@ConditionalOnMissingBean(RunningBackGroundExecutor.class)
public class RunningBackGroundExecutor {

    @XxlJob("runningBackGroundJobHandler")
    public void runningBackGroundJobHandler() {
        var jobs = SpringUtil.getBeansByTypeSafe(IBackgroundJob.class);
        if (CollUtil.isEmpty(jobs)) {
            XxlJobHelper.log("No background jobs found.");
            return;
        }
        for (var job : jobs) {
            XxlJobHelper.log("Executing background job: {} description: {}", job.name(), job.description());
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
