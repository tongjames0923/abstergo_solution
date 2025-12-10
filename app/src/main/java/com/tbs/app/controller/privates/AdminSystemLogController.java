package com.tbs.app.controller.privates;

import com.tbs.app.service.log.ISystemLogService;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 系统日志管理控制器
 * <p>
 * 提供系统日志相关的管理接口，用于查询和统计系统日志
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private/log/")
public class AdminSystemLogController extends BaseController {

    @Resource
    private ISystemLogService systemLogService;

    /**
     * 获取日志操作类型列表
     * <p>
     * 查询系统日志的操作类型列表，并统计每种类型的日志数量
     * </p>
     *
     * @return 日志操作类型列表及统计结果
     */
    @GetMapping("listWithOpType")
    public AjaxResult listWithOpType() {
        return successResult(systemLogService.getLogOperateTypeList(28));
    }

    /**
     * 获取业务名称
     * <p>
     * 实现BaseController接口的方法，返回当前控制器的业务名称
     * </p>
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "系统日志报表";
    }
}
