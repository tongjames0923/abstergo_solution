package com.tbs.app.controller.privates;

import com.tbs.app.service.log.ISystemLogService;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/private/log/")
public class AdminSystemLogController extends BaseController {

    @Resource
    private ISystemLogService systemLogService;

    @GetMapping("listWithOpType")
    public AjaxResult listWithOpType() {
        return successResult(systemLogService.getLogOperateTypeList(28));
    }

    @Override
    public String getBusinessName() {
        return "系统日志报表";
    }
}
