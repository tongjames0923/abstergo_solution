package com.tbs.app.controller.privates;

import com.tbs.app.service.version.IAppPackageService;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.pojo.criteria.AppPackageCriteria;
import com.tbs.shared.pojo.entity.version.AppPackageEntity;
import com.tbs.shared.pojo.model.PageVO;
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
import java.util.List;

/**
 * 应用安装包管理控制器
 * <p>
 * 提供应用版本下安装包产物的增删改查、按版本查询列表等管理接口；
 * 文件实体的上传与下载由统一对象存储接口（/private/oss/**）承担
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/private/admin/app-package")
@Tag(name = "应用安装包管理接口", description = "应用安装包产物管理与查询接口")
public class AppPackageController
    extends AbstractCrudController<AppPackageEntity, Long, AppPackageCriteria> {

    @Resource
    private IAppPackageService appPackageService;

    @Override
    public IAppPackageService getService() {
        return appPackageService;
    }

    /**
     * 根据ID获取安装包详情
     *
     * @param id 安装包ID
     * @return 安装包实体
     */
    @Override
    @GetMapping("get/{id}")
    @AjaxResponseType(AppPackageEntity.class)
    @Operation(summary = "获取安装包详情", description = "根据ID获取安装包详情")
    @ApiResponse(responseCode = "200", description = "成功获取安装包详情",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult get(@Parameter(name = "id", description = "安装包ID", required = true) @PathVariable Long id) {
        return super.get(id);
    }

    /**
     * 保存安装包（新建或编辑）
     *
     * @param entity 安装包实体
     * @return 保存后的安装包实体
     */
    @Override
    @PostMapping("save")
    @AjaxResponseType(AppPackageEntity.class)
    @Operation(summary = "保存安装包", description = "新建或编辑安装包信息")
    @ApiResponse(responseCode = "200", description = "成功保存安装包",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult save(@Parameter(name = "entity", description = "安装包实体",
        required = true) @RequestBody AppPackageEntity entity) {
        return super.save(entity);
    }

    /**
     * 删除安装包
     *
     * @param id 安装包ID
     * @return 删除结果
     */
    @Override
    @PostMapping("delete/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除安装包", description = "根据ID删除安装包")
    @ApiResponse(responseCode = "200", description = "成功删除安装包",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult delete(
        @Parameter(name = "id", description = "安装包ID", required = true) @PathVariable Long id) {
        return super.delete(id);
    }

    /**
     * 分页查询安装包列表
     *
     * @param criteria 查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 安装包分页列表
     */
    @Override
    @PostMapping("query")
    @AjaxResponseType(value = PageVO.class, genericArgs = {AppPackageEntity.class})
    @Operation(summary = "查询安装包列表", description = "分页查询安装包列表")
    @ApiResponse(responseCode = "200", description = "成功查询安装包列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult query(@Parameter(name = "criteria", description = "查询条件") @RequestBody(
            required = false) AppPackageCriteria criteria,
        @Parameter(name = "pageNum", description = "页码") @RequestParam(required = false,
            defaultValue = "1") Long pageNum,
        @Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false,
            defaultValue = "10") Long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

    /**
     * 按版本ID查询安装包列表
     *
     * @param versionId 版本ID
     * @return 该版本下的安装包列表
     */
    @GetMapping("listByVersion/{versionId}")
    @AjaxResponseType(value = List.class, genericArgs = {AppPackageEntity.class})
    @Operation(summary = "按版本查询安装包", description = "查询指定版本下的全部安装包")
    @ApiResponse(responseCode = "200", description = "成功查询版本下的安装包列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult listByVersion(
        @Parameter(name = "versionId", description = "版本ID", required = true) @PathVariable Long versionId) {
        return successResult(appPackageService.listByVersionId(versionId));
    }

    @Override
    public String getBusinessName() {
        return "应用安装包管理";
    }
}
