package com.tbs.app.service;

import com.tbs.shared.interfaces.log.ISystemOperateLogSender;
import com.tbs.struct.tree.closure.impl.service.ClosureTreeServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * XXL-Job任务执行器
 * <p>
 * 负责执行系统中的定时任务，包括树结构重建和日志数据刷新
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class AppExecutor {
    @Resource
    private ClosureTreeServiceImpl closureTreeService;

    @Resource
    @Qualifier("systemOperateLogSender")
    private ISystemOperateLogSender systemOperateLog;

    /**
     * 重建树结构任务
     * <p>
     * 使用XXL-Job调度，根据参数重建树的闭包表结构
     * </p>
     */
    @XxlJob("rebuildTree")
    public void rebuildTree() {
        XxlJobHelper.log(" rebuild tree closure start");
        closureTreeService.buildTreeClosure(XxlJobHelper.getJobParam());
        XxlJobHelper.log(" rebuild tree closure success");
    }

    /**
     * 刷新日志数据任务
     * <p>
     * 使用XXL-Job调度，将系统操作日志数据刷新到持久化存储
     * </p>
     */
    @XxlJob("flushLogData")
    public void flushLogData() {
        XxlJobHelper.log(" flush log data start");
        systemOperateLog.flush();
        XxlJobHelper.log(" flush log data success");
    }
}
