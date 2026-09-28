package com.tbs.app.controller.privates.version;

import com.tbs.app.service.version.IAppPackageService;
import com.tbs.app.service.version.IAppVersionService;
import com.tbs.app.vo.AppPackageVO;
import com.tbs.app.vo.AppVersionDetailVO;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.pojo.criteria.AppVersionCriteria;
import com.tbs.shared.pojo.entity.version.AppPackageEntity;
import com.tbs.shared.pojo.entity.version.AppVersionEntity;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用版本管理控制器
 * <p>
 * 提供应用版本的增删改查、生命周期状态流转（提交审核/发布/下线/回滚/归档）、 版本详情查询（含安装包列表）等管理接口
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/private/admin/app-version")
@Tag(name = "应用版本管理接口", description = "应用版本生命周期管理与查询接口")
public class AppVersionController extends AbstractCrudController<AppVersionEntity, Long, AppVersionCriteria> {

    @Resource
    private IAppVersionService appVersionService;

    @Resource
    private IAppPackageService appPackageService;

    @Override
    public IAppVersionService getService() {
        return appVersionService;
    }

    /**
     * 根据ID获取版本详情
     *
     * @param id 版本ID
     * @return 版本实体
     */
    @Override
    @GetMapping("get/{id}")
    @AjaxResponseType(AppVersionEntity.class)
    @Operation(summary = "获取版本详情", description = "根据ID获取应用版本详情")
    @ApiResponse(responseCode = "200", description = "成功获取版本详情",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.QUERY')")
    public AjaxResult get(@Parameter(name = "id", description = "版本ID", required = true) @PathVariable Long id) {
        return super.get(id);
    }

    /**
     * 保存版本（新建或编辑）
     *
     * @param entity 版本实体
     * @return 保存后的版本实体
     */
    @Override
    @PostMapping("save")
    @AjaxResponseType(AppVersionEntity.class)
    @Operation(summary = "保存版本", description = "新建或编辑应用版本，新建时初始为草稿状态")
    @ApiResponse(responseCode = "200", description = "成功保存版本",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.SAVE')")
    public AjaxResult save(
        @Parameter(name = "entity", description = "版本实体", required = true) @RequestBody AppVersionEntity entity) {
        return super.save(entity);
    }

    /**
     * 删除版本
     *
     * @param id 版本ID
     * @return 删除结果
     */
    @Override
    @PostMapping("delete/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除版本", description = "根据ID删除应用版本，仅草稿状态可删除")
    @ApiResponse(responseCode = "200", description = "成功删除版本",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.DELETE')")
    public AjaxResult delete(@Parameter(name = "id", description = "版本ID", required = true) @PathVariable Long id) {
        return super.delete(id);
    }

    /**
     * 分页查询版本列表
     *
     * @param criteria 查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 版本分页列表
     */
    @Override
    @PostMapping("query")
    @AjaxResponseType(value = PageVO.class, genericArgs = {AppVersionEntity.class})
    @Operation(summary = "查询版本列表", description = "分页查询应用版本列表")
    @ApiResponse(responseCode = "200", description = "成功查询版本列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.QUERY')")
    public AjaxResult query(@Parameter(name = "criteria", description = "查询条件") @RequestBody(
            required = false) AppVersionCriteria criteria,
        @Parameter(name = "pageNum", description = "页码") @RequestParam(required = false,
            defaultValue = "1") Long pageNum,
        @Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false,
            defaultValue = "10") Long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

    /**
     * 提交审核
     *
     * @param id 版本ID
     * @return 流转后的版本实体
     */
    @PostMapping("submit/{id}")
    @AjaxResponseType(AppVersionEntity.class)
    @Operation(summary = "提交审核", description = "将草稿状态的版本提交审核")
    @ApiResponse(responseCode = "200", description = "成功提交审核",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.SAVE')")
    public AjaxResult submit(@Parameter(name = "id", description = "版本ID", required = true) @PathVariable Long id) {
        return successResult(appVersionService.submit(id));
    }

    /**
     * 发布版本
     *
     * @param id 版本ID
     * @return 流转后的版本实体
     */
    @PostMapping("release/{id}")
    @AjaxResponseType(AppVersionEntity.class)
    @Operation(summary = "发布版本", description = "将待审核状态的版本正式发布")
    @ApiResponse(responseCode = "200", description = "成功发布版本",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.SAVE')")
    public AjaxResult release(@Parameter(name = "id", description = "版本ID", required = true) @PathVariable Long id) {
        return successResult(appVersionService.release(id));
    }

    /**
     * 下线版本
     *
     * @param id 版本ID
     * @return 流转后的版本实体
     */
    @PostMapping("offline/{id}")
    @AjaxResponseType(AppVersionEntity.class)
    @Operation(summary = "下线版本", description = "将已发布状态的版本下线")
    @ApiResponse(responseCode = "200", description = "成功下线版本",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.SAVE')")
    public AjaxResult offline(@Parameter(name = "id", description = "版本ID", required = true) @PathVariable Long id) {
        return successResult(appVersionService.offline(id));
    }

    /**
     * 回滚版本
     *
     * @param id 版本ID
     * @return 流转后的版本实体
     */
    @PostMapping("rollback/{id}")
    @AjaxResponseType(AppVersionEntity.class)
    @Operation(summary = "回滚版本", description = "将已发布状态的版本回滚")
    @ApiResponse(responseCode = "200", description = "成功回滚版本",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.SAVE')")
    public AjaxResult rollback(@Parameter(name = "id", description = "版本ID", required = true) @PathVariable Long id) {
        return successResult(appVersionService.rollback(id));
    }

    /**
     * 归档版本
     *
     * @param id 版本ID
     * @return 流转后的版本实体
     */
    @PostMapping("archive/{id}")
    @AjaxResponseType(AppVersionEntity.class)
    @Operation(summary = "归档版本", description = "将已下线状态的版本归档")
    @ApiResponse(responseCode = "200", description = "成功归档版本",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.SAVE')")
    public AjaxResult archive(@Parameter(name = "id", description = "版本ID", required = true) @PathVariable Long id) {
        return successResult(appVersionService.archive(id));
    }

    /**
     * 获取版本聚合详情（版本信息 + 安装包列表）
     *
     * @param id 版本ID
     * @return 版本聚合详情
     */
    @GetMapping("detail/{id}")
    @AjaxResponseType(AppVersionDetailVO.class)
    @Operation(summary = "获取版本聚合详情", description = "获取版本完整信息与名下安装包列表")
    @ApiResponse(responseCode = "200", description = "成功获取版本聚合详情",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.QUERY')")
    public AjaxResult getDetail(
        @Parameter(name = "id", description = "版本ID", required = true) @PathVariable Long id) {
        AppVersionEntity version = appVersionService.queryById(id);
        List<AppPackageEntity> packages = appPackageService.listByVersionId(id);
        AppVersionDetailVO vo = new AppVersionDetailVO();
        vo.setVersion(version);
        vo.setPackages(packages.stream().map(this::toPackageVO).collect(Collectors.toList()));
        return successResult(vo);
    }

    /**
     * 查询最新已发布版本
     *
     * @param appKey   应用标识
     * @param platform 平台标识
     * @return 最新已发布版本
     */
    @GetMapping("latest")
    @AjaxResponseType(AppVersionEntity.class)
    @Operation(summary = "查询最新已发布版本", description = "查询指定应用与平台下的最新已发布版本")
    @ApiResponse(responseCode = "200", description = "成功查询最新已发布版本",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.QUERY')")
    public AjaxResult getLatestReleased(
        @Parameter(name = "appKey", description = "应用标识", required = true) @RequestParam String appKey,
        @Parameter(name = "platform", description = "平台标识", required = true) @RequestParam String platform) {
        return successResult(appVersionService.queryLatestReleased(appKey, platform));
    }

    /**
     * 查询已登记版本的应用标识清单
     *
     * @return 去重后的应用标识列表
     */
    @GetMapping("appKeys")
    @AjaxResponseType(List.class)
    @Operation(summary = "查询应用标识清单", description = "查询已登记版本的应用标识去重列表，供兼容策略选择源/目标应用时使用")
    @ApiResponse(responseCode = "200", description = "成功查询应用标识清单",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.DEV.VERSION.QUERY')")
    public AjaxResult listAppKeys() {
        return successResult(appVersionService.listAppKeys());
    }

    /**
     * 将安装包实体转换为视图对象
     *
     * @param pkg 安装包实体
     * @return 安装包视图对象
     */
    private AppPackageVO toPackageVO(AppPackageEntity pkg) {
        AppPackageVO vo = new AppPackageVO();
        vo.setId(pkg.getId());
        vo.setVersionId(pkg.getVersionId());
        vo.setBucketName(pkg.getBucketName());
        vo.setObjectName(pkg.getObjectName());
        vo.setMd5(pkg.getMd5());
        vo.setDownloadUrl(pkg.getDownloadUrl());
        vo.setCreateTime(pkg.getCreateTime());
        return vo;
    }

    @Override
    public String getBusinessName() {
        return "应用版本管理";
    }
}
