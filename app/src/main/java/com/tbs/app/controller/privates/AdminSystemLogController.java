package com.tbs.app.controller.privates;

import com.tbs.app.annotation.NoLog;
import com.tbs.app.service.log.ISystemLogService;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.logging.core.ILogRecorder;
import com.tbs.logging.form.LoggingRecordExtractForm;
import com.tbs.common.data.PageVO;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @DubboReference(check = false, lazy = true)
    private ISystemLogService systemLogService;

    @Resource
    private ILogRecorder recorder;

    /**
     * 获取日志操作类型列表
     * <p>
     * 查询系统日志的操作类型列表,并统计每种类型的日志数量
     * </p>
     *
     * @return 日志操作类型列表及统计结果
     */
    @NoLog
    @GetMapping("listWithOpType")
    @AjaxResponseType(Object.class)
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult listWithOpType() {
        return successResult(systemLogService.getLogOperateTypeList(28));
    }

    /**
     * 获取所有操作类型列表(用于下拉框搜索)
     * <p>
     * 返回不重复的操作类型,支持按关键词模糊搜索
     * </p>
     *
     * @param keyword 关键词(可选)
     * @return 操作类型列表
     */
    @NoLog
    @GetMapping("operateTypes")
    @AjaxResponseType(Object.class)
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult getOperateTypes(@RequestParam(required = false) String keyword) {
        return successResult(systemLogService.getOperateTypeList(keyword));
    }

    /**
     * 获取日志记录分页列表
     * <p>
     * 根据查询条件，获取系统日志记录分页列表
     * </p>
     *
     * @param form 查询条件
     * @return 日志记录分页列表
     */
    @NoLog
    @PostMapping("listRecord")
    @AjaxResponseType(PageVO.class)
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult listRecord(@RequestBody LoggingRecordExtractForm form) {
        return successResult(systemLogService.getLogRecordList(form));
    }

    /**
     * 强制刷新日志记录
     * <p>
     * 强制刷新日志记录，使日志记录立即生效
     * </p>
     *
     * @return 操作结果
     */
    @PostMapping("forceFlush")
    @AjaxResponseType(Object.class)
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult forceFlush() {
        recorder.flush();
        return successResult();
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
