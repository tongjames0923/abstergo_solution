package com.tbs.app.service;

import com.tbs.struct.tree.impl.service.ClosureTreeServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TreeClosureExecutor {
    @Resource
    private ClosureTreeServiceImpl closureTreeService;

    @XxlJob("rebuildTree")
    public void rebuildTree() {
        //TODO: 树结构重建
        XxlJobHelper.log(" rebuild tree closure start");
        closureTreeService.buildTreeClosure();
        XxlJobHelper.log(" rebuild tree closure success");
    }
}
