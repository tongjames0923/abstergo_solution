package com.tbs.app.service;

import com.tbs.app.service.impl.company.CompanyTreeStructService;
import com.tbs.app.service.impl.department.DepartmentTreeStructService;
import com.tbs.logging.core.ILogRecorder;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

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
    private ILogRecorder systemOperateLog;

    @Resource
    CompanyTreeStructService companyTreeStructService;

    @Resource
    DepartmentTreeStructService departmentTreeStructService;

    @XxlJob("company_tree_rebuild")
    public void companyTreeRebuild() {
    }

    @XxlJob("department_tree_rebuild")
    public void departmentTreeRebuild() {
        departmentTreeStructService.rebuildTree();
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
