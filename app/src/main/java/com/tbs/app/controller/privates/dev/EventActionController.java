package com.tbs.app.controller.privates.dev;

import com.tbs.app.form.EventActionSaveForm;
import com.tbs.app.form.EventActionTestForm;
import com.tbs.app.service.event.ActionConfigValidator;
import com.tbs.app.service.event.IEventActionService;
import com.tbs.app.vo.EventActionTestResultVO;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.common.data.PageVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.script.api.expressible.impl.TriggerActionQueryService;
import com.tbs.script.api.expressible.vo.ActionMeta;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.EventActionCriteria;
import com.tbs.shared.pojo.entity.event.SystemEventActionEntity;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 事件动作管理控制器。
 *
 * <p>提供可选动作元数据查询、动作绑定的增删改查与启停，以及单动作测试执行入口；
 * 保存前校验动作是否已在脚本模块注册。</p>
 *
 * @author abstergo
 */
@RestController
@RequestMapping("private/event-action")
@Tag(name = "事件动作管理接口", description = "事件动作绑定、启停与测试执行接口")
public class EventActionController
    extends AbstractCrudController<SystemEventActionEntity, Long, EventActionCriteria> {

    /**
     * 动作绑定服务
     */
    @Resource
    private IEventActionService eventActionService;

    /**
     * 动作元数据查询服务
     */
    @Resource
    private TriggerActionQueryService triggerActionQueryService;

    /**
     * 动作绑定配置校验器
     */
    @Resource
    private ActionConfigValidator actionConfigValidator;

    @Override
    public IServiceTemplate<SystemEventActionEntity, Long> getService() {
        return eventActionService;
    }

    /**
     * 查询全部可选动作元信息。
     *
     * @return 动作元信息列表
     */
    @GetMapping("actions")
    @AjaxResponseType(value = List.class, genericArgs = {ActionMeta.class})
    @Operation(summary = "查询动作", description = "列出已注册的触发器动作及其参数声明，供动作编排选择与渲染表单")
    @ApiResponse(responseCode = "200", description = "成功查询动作",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.ACTION.QUERY')")
    public AjaxResult actions() {
        return successResult(triggerActionQueryService.listActions());
    }

    /**
     * 按事件查询动作绑定（含停用）。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 动作绑定列表
     */
    @GetMapping("by-event")
    @AjaxResponseType(value = List.class, genericArgs = {SystemEventActionEntity.class})
    @Operation(summary = "按事件查询动作", description = "查询指定事件下的全部动作绑定，供编排页回显")
    @ApiResponse(responseCode = "200", description = "成功查询动作绑定",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.ACTION.QUERY')")
    public AjaxResult byEvent(
        @Parameter(name = "reporterId", description = "事件上报器ID", required = true) @RequestParam String reporterId,
        @Parameter(name = "eventCode", description = "事件编码", required = true) @RequestParam String eventCode) {
        return successResult(eventActionService.listByEvent(reporterId, eventCode));
    }

    /**
     * 整体保存指定事件下的动作绑定。
     *
     * @param form 保存表单
     * @return 保存后的动作绑定列表
     */
    @PostMapping("save-event")
    @AjaxResponseType(value = List.class, genericArgs = {SystemEventActionEntity.class})
    @Operation(summary = "保存事件动作", description = "整体替换指定事件下的动作绑定，按动作增量合并")
    @ApiResponse(responseCode = "200", description = "成功保存事件动作",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.ACTION.SAVE')")
    public AjaxResult saveEvent(
        @Parameter(name = "form", description = "动作保存表单", required = true) @RequestBody EventActionSaveForm form) {
        AssertUtil.isTrue(form == null).i18n("event.action.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        actionConfigValidator.assertBindings(form.getActions());
        return successResult(
            eventActionService.replaceByEvent(form.getReporterId(), form.getEventCode(), form.getActions()));
    }

    /**
     * 测试执行单个动作。
     *
     * @param form 测试表单
     * @return 执行回执
     */
    @PostMapping("test")
    @AjaxResponseType(EventActionTestResultVO.class)
    @Operation(summary = "测试执行动作", description = "按动作ID与参数值执行一次动作，返回成功状态与耗时")
    @ApiResponse(responseCode = "200", description = "成功执行动作",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.ACTION.QUERY')")
    public AjaxResult test(
        @Parameter(name = "form", description = "动作测试表单", required = true) @RequestBody EventActionTestForm form) {
        AssertUtil.isTrue(form == null).i18n("event.action.test.form.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        actionConfigValidator.assertActionExists(form.getActionId());
        long start = System.currentTimeMillis();
        boolean success = triggerActionQueryService.execute(form.getActionId(), form.getParamValues());
        return successResult(EventActionTestResultVO.builder().actionId(form.getActionId()).success(success)
            .elapsedMs(System.currentTimeMillis() - start)
            .errorMsg(success ? null : I18nUtil.get("event.action.test.failed")).build());
    }

    /**
     * 保存单个动作绑定。
     *
     * @param entity 动作绑定实体
     * @return 保存后的动作绑定
     */
    @Override
    @PostMapping("save")
    @AjaxResponseType(SystemEventActionEntity.class)
    @Operation(summary = "保存单条动作", description = "新建或更新单条动作绑定，保存前校验动作已注册")
    @ApiResponse(responseCode = "200", description = "成功保存动作绑定",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.ACTION.SAVE')")
    public AjaxResult save(
        @Parameter(name = "entity", description = "动作绑定实体", required = true) @RequestBody SystemEventActionEntity entity) {
        if (entity != null) {
            actionConfigValidator.assertActionExists(entity.getActionId());
        }
        return super.save(entity);
    }

    /**
     * 删除动作绑定。
     *
     * @param id 绑定主键
     * @return 删除结果
     */
    @Override
    @PostMapping("delete/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除动作绑定", description = "根据主键逻辑删除动作绑定")
    @ApiResponse(responseCode = "200", description = "成功删除动作绑定",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.ACTION.DELETE')")
    public AjaxResult delete(
        @Parameter(name = "id", description = "绑定主键", required = true) @PathVariable Long id) {
        return super.delete(id);
    }

    /**
     * 分页查询动作绑定。
     *
     * @param criteria 查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 动作绑定分页列表
     */
    @Override
    @PostMapping("query")
    @AjaxResponseType(value = PageVO.class, genericArgs = {SystemEventActionEntity.class})
    @Operation(summary = "查询动作绑定列表", description = "分页查询动作绑定，支持上报器、事件、动作与启停过滤")
    @ApiResponse(responseCode = "200", description = "成功查询动作绑定列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.ACTION.QUERY')")
    public AjaxResult query(@Parameter(name = "criteria", description = "查询条件") @RequestBody(
            required = false) EventActionCriteria criteria,
        @Parameter(name = "pageNum", description = "页码") @RequestParam(required = false,
            defaultValue = "1") Long pageNum,
        @Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false,
            defaultValue = "10") Long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

    /**
     * 启用动作绑定。
     *
     * @param id 绑定主键
     * @return 操作结果
     */
    @PostMapping("enable/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "启用动作绑定", description = "启用指定动作绑定")
    @ApiResponse(responseCode = "200", description = "成功启用动作绑定",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.ACTION.SAVE')")
    public AjaxResult enable(
        @Parameter(name = "id", description = "绑定主键", required = true) @PathVariable Long id) {
        eventActionService.setEnabled(id, true);
        return successResult(null);
    }

    /**
     * 停用动作绑定。
     *
     * @param id 绑定主键
     * @return 操作结果
     */
    @PostMapping("disable/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "停用动作绑定", description = "停用指定动作绑定")
    @ApiResponse(responseCode = "200", description = "成功停用动作绑定",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.ACTION.SAVE')")
    public AjaxResult disable(
        @Parameter(name = "id", description = "绑定主键", required = true) @PathVariable Long id) {
        eventActionService.setEnabled(id, false);
        return successResult(null);
    }

    @Override
    public String getBusinessName() {
        return "事件动作管理";
    }
}
