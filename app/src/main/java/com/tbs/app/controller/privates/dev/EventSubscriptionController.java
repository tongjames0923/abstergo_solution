package com.tbs.app.controller.privates.dev;

import com.tbs.app.form.EventSubscriptionSaveForm;
import com.tbs.app.service.condition.IConditionRuleService;
import com.tbs.app.service.event.EventReportSupport;
import com.tbs.app.service.event.IEventSubscriptionService;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.common.data.PageVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.script.api.expressible.impl.EventReporterQueryService;
import com.tbs.script.api.expressible.impl.TriggerQueryService;
import com.tbs.script.api.expressible.orchestration.vo.EventEnvelope;
import com.tbs.script.api.expressible.orchestration.vo.EventReportResult;
import com.tbs.script.api.expressible.vo.EventMeta;
import com.tbs.script.api.expressible.vo.TriggerMeta;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.EventSubscriptionCriteria;
import com.tbs.shared.pojo.entity.event.SystemEventSubscriptionEntity;
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
 * 事件订阅管理控制器。
 *
 * <p>提供「上报器 / 事件 / 触发器」元数据查询、订阅配置的增删改查与启停，
 * 以及模拟事件上报调试入口；条件规则编码在保存前校验存在性。</p>
 *
 * @author abstergo
 */
@RestController
@RequestMapping("private/event-subscription")
@Tag(name = "事件订阅管理接口", description = "事件订阅绑定、启停与调试上报接口")
public class EventSubscriptionController
    extends AbstractCrudController<SystemEventSubscriptionEntity, Long, EventSubscriptionCriteria> {

    /**
     * 订阅服务
     */
    @Resource
    private IEventSubscriptionService eventSubscriptionService;

    /**
     * 事件元数据查询服务
     */
    @Resource
    private EventReporterQueryService eventReporterQueryService;

    /**
     * 触发器元数据查询服务
     */
    @Resource
    private TriggerQueryService triggerQueryService;

    /**
     * 条件规则服务
     */
    @Resource
    private IConditionRuleService conditionRuleService;

    /**
     * 事件上报门面
     */
    @Resource
    private EventReportSupport eventReportSupport;

    @Override
    public IServiceTemplate<SystemEventSubscriptionEntity, Long> getService() {
        return eventSubscriptionService;
    }

    /**
     * 查询全部事件上报器摘要。
     *
     * @return 上报器摘要列表
     */
    @GetMapping("reporters")
    @AjaxResponseType(value = List.class, genericArgs = {EventReporterQueryService.ReporterSummary.class})
    @Operation(summary = "查询事件上报器", description = "列出已注册的事件上报器摘要，供订阅编排选择事件来源")
    @ApiResponse(responseCode = "200", description = "成功查询事件上报器",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.SUBSCRIPTION.QUERY')")
    public AjaxResult reporters() {
        return successResult(eventReporterQueryService.listReporters());
    }

    /**
     * 查询指定上报器下的事件定义。
     *
     * @param reporterId 事件上报器ID
     * @return 事件元信息列表
     */
    @GetMapping("events")
    @AjaxResponseType(value = List.class, genericArgs = {EventMeta.class})
    @Operation(summary = "查询事件定义", description = "按上报器查询事件及其参数声明，用于渲染模拟上报表单")
    @ApiResponse(responseCode = "200", description = "成功查询事件定义",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.SUBSCRIPTION.QUERY')")
    public AjaxResult events(
        @Parameter(name = "reporterId", description = "事件上报器ID", required = true) @RequestParam String reporterId) {
        return successResult(eventReporterQueryService.listEvents(reporterId));
    }

    /**
     * 查询全部触发器元信息。
     *
     * @return 触发器元信息列表
     */
    @GetMapping("triggers")
    @AjaxResponseType(value = List.class, genericArgs = {TriggerMeta.class})
    @Operation(summary = "查询触发器", description = "列出已注册的触发器，供订阅绑定选择触发器")
    @ApiResponse(responseCode = "200", description = "成功查询触发器",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.SUBSCRIPTION.QUERY')")
    public AjaxResult triggers() {
        return successResult(triggerQueryService.listTriggers());
    }

    /**
     * 按事件查询订阅列表（含停用）。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 订阅列表
     */
    @GetMapping("by-event")
    @AjaxResponseType(value = List.class, genericArgs = {SystemEventSubscriptionEntity.class})
    @Operation(summary = "按事件查询订阅", description = "查询指定事件下的全部订阅，供编排页回显")
    @ApiResponse(responseCode = "200", description = "成功查询事件订阅",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.SUBSCRIPTION.QUERY')")
    public AjaxResult byEvent(
        @Parameter(name = "reporterId", description = "事件上报器ID", required = true) @RequestParam String reporterId,
        @Parameter(name = "eventCode", description = "事件编码", required = true) @RequestParam String eventCode) {
        return successResult(eventSubscriptionService.listByEvent(reporterId, eventCode));
    }

    /**
     * 整体保存指定事件下的订阅。
     *
     * @param form 保存表单
     * @return 保存后的订阅列表
     */
    @PostMapping("save-event")
    @AjaxResponseType(value = List.class, genericArgs = {SystemEventSubscriptionEntity.class})
    @Operation(summary = "保存事件订阅", description = "整体替换指定事件下的订阅，按触发器增量合并")
    @ApiResponse(responseCode = "200", description = "成功保存事件订阅",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.SUBSCRIPTION.SAVE')")
    public AjaxResult saveEvent(
        @Parameter(name = "form", description = "订阅保存表单", required = true) @RequestBody EventSubscriptionSaveForm form) {
        AssertUtil.isTrue(form == null).i18n("event.subscription.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        if (CollUtil.isNotEmpty(form.getSubscriptions())) {
            form.getSubscriptions().forEach(this::validateBinding);
        }
        return successResult(
            eventSubscriptionService.replaceByEvent(form.getReporterId(), form.getEventCode(), form.getSubscriptions()));
    }

    /**
     * 模拟上报事件并返回分发明细。
     *
     * @param envelope 事件信封
     * @return 上报与分发结果
     */
    @PostMapping("report")
    @AjaxResponseType(EventReportResult.class)
    @Operation(summary = "调试上报事件", description = "按信封上报模拟事件，返回分发明细与耗时")
    @ApiResponse(responseCode = "200", description = "成功上报事件",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.SUBSCRIPTION.QUERY')")
    public AjaxResult report(
        @Parameter(name = "envelope", description = "事件信封", required = true) @RequestBody EventEnvelope envelope) {
        return successResult(eventReportSupport.report(envelope.getReporterId(), envelope.getEventCode(),
            envelope.getParamValues()));
    }

    /**
     * 保存单个订阅。
     *
     * @param entity 订阅实体
     * @return 保存后的订阅
     */
    @Override
    @PostMapping("save")
    @AjaxResponseType(SystemEventSubscriptionEntity.class)
    @Operation(summary = "保存单条订阅", description = "新建或更新单条事件订阅，保存前校验触发器与条件规则存在性")
    @ApiResponse(responseCode = "200", description = "成功保存订阅",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.SUBSCRIPTION.SAVE')")
    public AjaxResult save(
        @Parameter(name = "entity", description = "订阅实体", required = true) @RequestBody SystemEventSubscriptionEntity entity) {
        validateBinding(entity);
        return super.save(entity);
    }

    /**
     * 删除订阅。
     *
     * @param id 订阅主键
     * @return 删除结果
     */
    @Override
    @PostMapping("delete/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除订阅", description = "根据主键逻辑删除事件订阅")
    @ApiResponse(responseCode = "200", description = "成功删除订阅",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.SUBSCRIPTION.DELETE')")
    public AjaxResult delete(
        @Parameter(name = "id", description = "订阅主键", required = true) @PathVariable Long id) {
        return super.delete(id);
    }

    /**
     * 分页查询订阅。
     *
     * @param criteria 查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 订阅分页列表
     */
    @Override
    @PostMapping("query")
    @AjaxResponseType(value = PageVO.class, genericArgs = {SystemEventSubscriptionEntity.class})
    @Operation(summary = "查询订阅列表", description = "分页查询事件订阅，支持上报器、事件、触发器与启停过滤")
    @ApiResponse(responseCode = "200", description = "成功查询订阅列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.SUBSCRIPTION.QUERY')")
    public AjaxResult query(@Parameter(name = "criteria", description = "查询条件") @RequestBody(
            required = false) EventSubscriptionCriteria criteria,
        @Parameter(name = "pageNum", description = "页码") @RequestParam(required = false,
            defaultValue = "1") Long pageNum,
        @Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false,
            defaultValue = "10") Long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

    /**
     * 启用订阅。
     *
     * @param id 订阅主键
     * @return 操作结果
     */
    @PostMapping("enable/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "启用订阅", description = "启用指定事件订阅")
    @ApiResponse(responseCode = "200", description = "成功启用订阅",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.SUBSCRIPTION.SAVE')")
    public AjaxResult enable(
        @Parameter(name = "id", description = "订阅主键", required = true) @PathVariable Long id) {
        eventSubscriptionService.setEnabled(id, true);
        return successResult(null);
    }

    /**
     * 停用订阅。
     *
     * @param id 订阅主键
     * @return 操作结果
     */
    @PostMapping("disable/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "停用订阅", description = "停用指定事件订阅")
    @ApiResponse(responseCode = "200", description = "成功停用订阅",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.EVENT.SUBSCRIPTION.SAVE')")
    public AjaxResult disable(
        @Parameter(name = "id", description = "订阅主键", required = true) @PathVariable Long id) {
        eventSubscriptionService.setEnabled(id, false);
        return successResult(null);
    }

    /**
     * 校验订阅引用的触发器与条件规则均存在。
     *
     * @param entity 订阅实体
     */
    private void validateBinding(SystemEventSubscriptionEntity entity) {
        if (entity == null) {
            return;
        }
        if (StrUtil.isNotBlank(entity.getTriggerId())) {
            AssertUtil.isTrue(triggerQueryService.getTrigger(entity.getTriggerId()) == null)
                .i18n("event.subscription.trigger.not.registered", entity.getTriggerId())
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        }
        if (StrUtil.isNotBlank(entity.getRuleCode())) {
            AssertUtil.isTrue(conditionRuleService.queryByCode(entity.getRuleCode()) == null)
                .i18n("event.subscription.rule.not.exists", entity.getRuleCode())
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        }
    }

    @Override
    public String getBusinessName() {
        return "事件订阅管理";
    }
}
