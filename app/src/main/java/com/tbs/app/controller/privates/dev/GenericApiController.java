package com.tbs.app.controller.privates.dev;

import com.tbs.app.service.genericapi.IGenericApiService;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.common.data.PageVO;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.pojo.criteria.CommonGenericApiCriteria;
import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.stream.Collectors;

/**
 * 泛化API管理控制器
 * <p>
 * 提供泛化API配置的增删改查及启停用功能
 * </p>
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/private/admin/generic-api")
@Tag(name = "泛化API管理接口", description = "泛化API配置管理相关操作接口")
public class GenericApiController
    extends AbstractCrudController<SystemGenericApiEntity, Long, CommonGenericApiCriteria> {

    @Resource
    private IGenericApiService genericApiService;

    @Override
    public IGenericApiService getService() {
        return genericApiService;
    }

    /**
     * 根据ID获取泛化API配置
     *
     * @param id API配置ID
     * @return API配置信息
     */
    @Override
    @GetMapping("get/{id}")
    @AjaxResponseType(SystemGenericApiEntity.class)
    @Operation(summary = "获取泛化API配置", description = "根据ID获取泛化API配置详情")
    @ApiResponse(responseCode = "200", description = "成功获取泛化API配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult get(@Parameter(name = "id", description = "API配置ID", required = true) @PathVariable Long id) {
        return super.get(id);
    }

    /**
     * 保存泛化API配置
     *
     * @param entity API配置信息
     * @return 保存后的API配置信息
     */
    @Override
    @PostMapping("save")
    @AjaxResponseType(SystemGenericApiEntity.class)
    @Operation(summary = "保存泛化API配置", description = "创建或更新泛化API配置")
    @ApiResponse(responseCode = "200", description = "成功保存泛化API配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult save(@Parameter(name = "entity", description = "API配置信息",
        required = true) @RequestBody SystemGenericApiEntity entity) {
        return super.save(entity);
    }

    /**
     * 删除泛化API配置
     *
     * @param id API配置ID
     * @return 删除结果
     */
    @Override
    @PostMapping("delete/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除泛化API配置", description = "根据ID删除泛化API配置")
    @ApiResponse(responseCode = "200", description = "成功删除泛化API配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult delete(
        @Parameter(name = "id", description = "API配置ID", required = true) @PathVariable Long id) {
        return super.delete(id);
    }

    /**
     * 查询泛化API配置列表
     *
     * @param criteria 查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return API配置列表分页结果
     */
    @Override
    @PostMapping("query")
    @AjaxResponseType(value = PageVO.class, genericArgs = {SystemGenericApiEntity.class})
    @Operation(summary = "查询泛化API配置列表", description = "分页查询泛化API配置列表")
    @ApiResponse(responseCode = "200", description = "成功查询泛化API配置列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult query(@Parameter(name = "criteria", description = "查询条件") @RequestBody(
            required = false) CommonGenericApiCriteria criteria,
        @Parameter(name = "pageNum", description = "页码") @RequestParam(required = false,
            defaultValue = "1") Long pageNum,
        @Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false,
            defaultValue = "10") Long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

    /**
     * 启用泛化API
     *
     * @param apiId API ID
     * @return 操作结果
     */
    @PostMapping("/enable/{apiId}")
    @AjaxResponseType(void.class)
    @Operation(summary = "启用泛化API", description = "启用指定的泛化API")
    @ApiResponse(responseCode = "200", description = "成功启用泛化API",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult enable(
        @Parameter(name = "apiId", description = "API ID", required = true) @PathVariable String apiId) {
        genericApiService.enableApi(apiId);
        return successResult(null);
    }

    /**
     * 禁用泛化API
     *
     * @param apiId API ID
     * @return 操作结果
     */
    @PostMapping("/disable/{apiId}")
    @AjaxResponseType(void.class)
    @Operation(summary = "禁用泛化API", description = "禁用指定的泛化API")
    @ApiResponse(responseCode = "200", description = "成功禁用泛化API",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult disable(
        @Parameter(name = "apiId", description = "API ID", required = true) @PathVariable String apiId) {
        genericApiService.disableApi(apiId);
        return successResult(null);
    }

    /**
     * 获取所有可用的预授权列表
     *
     * @return 预授权列表
     */
    @GetMapping("allPreAuth")
    public AjaxResult getAllPreAuth() {
        return successResult(
            genericApiService.getAllAvaliablePreAuthList().stream().map(PreAuthorize -> PreAuthorize.getPreAuthId())
                .collect(Collectors.toList()));
    }

    @Override
    public String getBusinessName() {
        return "泛化接口管理";
    }
}
