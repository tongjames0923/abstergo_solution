package com.tbs.app.controller.privates.version;

import com.tbs.app.form.AppVersionCheckForm;
import com.tbs.app.service.version.IAppVersionCompatibilityPolicyService;
import com.tbs.app.vo.AppVersionUpdateCheckVO;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.pojo.criteria.AppVersionCompatibilityPolicyCriteria;
import com.tbs.shared.pojo.entity.version.AppVersionCompatibilityPolicyEntity;
import com.tbs.common.data.PageVO;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 应用版本兼容策略管理控制器
 * <p>
 * 提供跨应用版本兼容策略的增删改查、启停用、客户端升级检查等管理接口
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/private/admin/app-version-policy")
@Tag(name = "应用版本兼容策略管理接口", description = "跨应用版本兼容策略管理与升级检查接口")
public class AppVersionCompatibilityPolicyController
    extends AbstractCrudController<AppVersionCompatibilityPolicyEntity, Long, AppVersionCompatibilityPolicyCriteria> {

    @Resource
    private IAppVersionCompatibilityPolicyService policyService;

    @Override
    public IAppVersionCompatibilityPolicyService getService() {
        return policyService;
    }

    /**
     * 根据ID获取策略详情
     *
     * @param id 策略ID
     * @return 策略实体
     */
    @Override
    @GetMapping("get/{id}")
    @AjaxResponseType(AppVersionCompatibilityPolicyEntity.class)
    @Operation(summary = "获取策略详情", description = "根据ID获取兼容策略详情")
    @ApiResponse(responseCode = "200", description = "成功获取策略详情",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.POLICY.QUERY')")
    public AjaxResult get(@Parameter(name = "id", description = "策略ID", required = true) @PathVariable Long id) {
        return super.get(id);
    }

    /**
     * 保存策略（新建或编辑）
     *
     * @param entity 策略实体
     * @return 保存后的策略实体
     */
    @Override
    @PostMapping("save")
    @AjaxResponseType(AppVersionCompatibilityPolicyEntity.class)
    @Operation(summary = "保存策略", description = "新建或编辑兼容策略")
    @ApiResponse(responseCode = "200", description = "成功保存策略",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.POLICY.SAVE')")
    public AjaxResult save(@Parameter(name = "entity", description = "策略实体",
        required = true) @RequestBody AppVersionCompatibilityPolicyEntity entity) {
        return super.save(entity);
    }

    /**
     * 删除策略
     *
     * @param id 策略ID
     * @return 删除结果
     */
    @Override
    @PostMapping("delete/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除策略", description = "根据ID删除兼容策略")
    @ApiResponse(responseCode = "200", description = "成功删除策略",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.POLICY.DELETE')")
    public AjaxResult delete(
        @Parameter(name = "id", description = "策略ID", required = true) @PathVariable Long id) {
        return super.delete(id);
    }

    /**
     * 分页查询策略列表
     *
     * @param criteria 查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 策略分页列表
     */
    @Override
    @PostMapping("query")
    @AjaxResponseType(value = PageVO.class, genericArgs = {AppVersionCompatibilityPolicyEntity.class})
    @Operation(summary = "查询策略列表", description = "分页查询兼容策略列表")
    @ApiResponse(responseCode = "200", description = "成功查询策略列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.POLICY.QUERY')")
    public AjaxResult query(@Parameter(name = "criteria", description = "查询条件") @RequestBody(
            required = false) AppVersionCompatibilityPolicyCriteria criteria,
        @Parameter(name = "pageNum", description = "页码") @RequestParam(required = false,
            defaultValue = "1") Long pageNum,
        @Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false,
            defaultValue = "10") Long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

    /**
     * 启用兼容策略
     *
     * @param id 策略ID
     * @return 操作结果
     */
    @PostMapping("enable/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "启用策略", description = "启用指定的兼容策略")
    @ApiResponse(responseCode = "200", description = "成功启用策略",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.POLICY.SAVE')")
    public AjaxResult enable(
        @Parameter(name = "id", description = "策略ID", required = true) @PathVariable Long id) {
        policyService.enablePolicy(id);
        return successResult(null);
    }

    /**
     * 禁用兼容策略
     *
     * @param id 策略ID
     * @return 操作结果
     */
    @PostMapping("disable/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "禁用策略", description = "禁用指定的兼容策略")
    @ApiResponse(responseCode = "200", description = "成功禁用策略",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.POLICY.SAVE')")
    public AjaxResult disable(
        @Parameter(name = "id", description = "策略ID", required = true) @PathVariable Long id) {
        policyService.disablePolicy(id);
        return successResult(null);
    }

    /**
     * 客户端升级检查
     * <p>
     * 客户端上报自身应用标识、平台与当前版本号，服务端返回是否需要升级及最新版本信息
     *
     * @param form 升级检查入参
     * @return 升级检查结果
     */
    @PostMapping("check")
    @AjaxResponseType(AppVersionUpdateCheckVO.class)
    @Operation(summary = "升级检查", description = "客户端上报版本信息，检查是否需要升级")
    @ApiResponse(responseCode = "200", description = "成功执行升级检查",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.POLICY.QUERY')")
    public AjaxResult checkUpgrade(@Parameter(name = "form", description = "升级检查入参",
        required = true) @RequestBody AppVersionCheckForm form) {
        return successResult(
            policyService.evaluateCompatibility(form.getAppKey(), form.getPlatform(), form.getCurrentVersionCode()));
    }

    @Override
    public String getBusinessName() {
        return "应用版本兼容策略管理";
    }
}
