package com.tbs.app.service;

import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.interfaces.log.ISystemOperateLog;
import com.tbs.struct.tree.closure.impl.service.ClosureTreeServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class AppExecutor {
    @Resource
    private ClosureTreeServiceImpl closureTreeService;

    /**
     * 重建树结构
     */
    @XxlJob("rebuildTree")
    public void rebuildTree() {
        XxlJobHelper.log(" rebuild tree closure start");
        closureTreeService.buildTreeClosure(XxlJobHelper.getJobParam());
        XxlJobHelper.log(" rebuild tree closure success");
    }

    /**
     * 刷新日志数据
     */
    @XxlJob("flushLogData")
    public void flushLogData() {
        XxlJobHelper.log(" flush log data start");
        ISystemOperateLog systemOperateLog = SpringUtil.getBean(ISystemOperateLog.class);
        systemOperateLog.flush();
        XxlJobHelper.log(" flush log data success");
    }
}
