package com.tbs.app.controller.privates.dev;

import com.tbs.app.service.ConditionRuleEvaluator;
import com.tbs.app.service.condition.IConditionRuleService;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.common.data.PageVO;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.pojo.criteria.ConditionRuleCriteria;
import com.tbs.shared.pojo.entity.condition.SystemConditionRuleEntity;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 条件规则管理控制器。
 *
 * <p>提供条件规则的增删改查、启停与按编码执行接口；执行结果只返回布尔值，
 * 规则不可用时统一为 false。</p>
 *
 * @author abstergo
 */
@RestController
@RequestMapping("private/condition-rule")
@Tag(name = "条件规则管理接口", description = "条件规则持久化与按编码执行接口")
public class ConditionRuleController
    extends AbstractCrudController<SystemConditionRuleEntity, Long, ConditionRuleCriteria> {

    /**
     * 条件规则服务
     */
    @Resource
    private IConditionRuleService conditionRuleService;

    /**
     * 条件规则求值器
     */
    @Resource
    private ConditionRuleEvaluator conditionRuleEvaluator;

    @Override
    public IConditionRuleService getService() {
        return conditionRuleService;
    }

    /**
     * 根据主键获取规则详情。
     *
     * @param id 规则主键
     * @return 规则实体
     */
    @Override
    @GetMapping("get/{id}")
    @AjaxResponseType(SystemConditionRuleEntity.class)
    @Operation(summary = "获取规则详情", description = "根据主键获取条件规则详情")
    @ApiResponse(responseCode = "200", description = "成功获取规则详情",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.CONDITION.RULE.QUERY')")
    public AjaxResult get(@Parameter(name = "id", description = "规则主键", required = true) @PathVariable Long id) {
        return super.get(id);
    }

    /**
     * 根据编码获取规则详情。
     *
     * @param ruleCode 规则编码
     * @return 规则实体
     */
    @GetMapping("get-by-code/{ruleCode}")
    @AjaxResponseType(SystemConditionRuleEntity.class)
    @Operation(summary = "按编码获取规则", description = "根据规则编码获取条件规则详情，供调试页与编辑器加载")
    @ApiResponse(responseCode = "200", description = "成功获取规则",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.CONDITION.RULE.QUERY')")
    public AjaxResult getByCode(
        @Parameter(name = "ruleCode", description = "规则编码", required = true) @PathVariable String ruleCode) {
        return successResult(conditionRuleService.queryByCode(ruleCode));
    }

    /**
     * 保存规则（新建或编辑）。
     *
     * @param entity 规则实体
     * @return 保存后的规则
     */
    @Override
    @PostMapping("save")
    @AjaxResponseType(SystemConditionRuleEntity.class)
    @Operation(summary = "保存规则", description = "新建或编辑条件规则，保存前校验取值器是否已注册")
    @ApiResponse(responseCode = "200", description = "成功保存规则",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.CONDITION.RULE.SAVE')")
    public AjaxResult save(@Parameter(name = "entity", description = "规则实体",
        required = true) @RequestBody SystemConditionRuleEntity entity) {
        if (entity != null) {
            conditionRuleEvaluator.assertGetterExists(entity.getGetterId());
        }
        return super.save(entity);
    }

    /**
     * 删除规则。
     *
     * @param id 规则主键
     * @return 删除结果
     */
    @Override
    @PostMapping("delete/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除规则", description = "根据主键逻辑删除条件规则")
    @ApiResponse(responseCode = "200", description = "成功删除规则",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.CONDITION.RULE.DELETE')")
    public AjaxResult delete(
        @Parameter(name = "id", description = "规则主键", required = true) @PathVariable Long id) {
        return super.delete(id);
    }

    /**
     * 分页查询规则列表。
     *
     * @param criteria 查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 规则分页列表
     */
    @Override
    @PostMapping("query")
    @AjaxResponseType(value = PageVO.class, genericArgs = {SystemConditionRuleEntity.class})
    @Operation(summary = "查询规则列表", description = "分页查询条件规则列表，支持编码、名称与启用状态过滤")
    @ApiResponse(responseCode = "200", description = "成功查询规则列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.CONDITION.RULE.QUERY')")
    public AjaxResult query(@Parameter(name = "criteria", description = "查询条件") @RequestBody(
            required = false) ConditionRuleCriteria criteria,
        @Parameter(name = "pageNum", description = "页码") @RequestParam(required = false,
            defaultValue = "1") Long pageNum,
        @Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false,
            defaultValue = "10") Long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

    /**
     * 启用规则。
     *
     * @param id 规则主键
     * @return 操作结果
     */
    @PostMapping("enable/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "启用规则", description = "启用指定的条件规则")
    @ApiResponse(responseCode = "200", description = "成功启用规则",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.CONDITION.RULE.SAVE')")
    public AjaxResult enable(
        @Parameter(name = "id", description = "规则主键", required = true) @PathVariable Long id) {
        conditionRuleService.setEnabled(id, true);
        return successResult(null);
    }

    /**
     * 停用规则。
     *
     * @param id 规则主键
     * @return 操作结果
     */
    @PostMapping("disable/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "停用规则", description = "停用指定的条件规则")
    @ApiResponse(responseCode = "200", description = "成功停用规则",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.CONDITION.RULE.SAVE')")
    public AjaxResult disable(
        @Parameter(name = "id", description = "规则主键", required = true) @PathVariable Long id) {
        conditionRuleService.setEnabled(id, false);
        return successResult(null);
    }

    /**
     * 按编码执行规则。
     *
     * @param ruleCode 规则编码
     * @return 布尔结果
     */
    @PostMapping("evaluate/{ruleCode}")
    @AjaxResponseType(Boolean.class)
    @Operation(summary = "执行规则", description = "按规则编码执行条件规则并返回布尔结果")
    @ApiResponse(responseCode = "200", description = "成功执行规则",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.CONDITION.RULE.QUERY')")
    public AjaxResult evaluate(
        @Parameter(name = "ruleCode", description = "规则编码", required = true) @PathVariable String ruleCode) {
        return successResult(conditionRuleEvaluator.evaluate(ruleCode));
    }

    @Override
    public String getBusinessName() {
        return "条件规则管理";
    }
}
