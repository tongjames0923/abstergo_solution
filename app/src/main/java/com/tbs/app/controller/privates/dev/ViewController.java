package com.tbs.app.controller.privates.dev;

import com.tbs.app.config.property.GatewayProperty;
import com.tbs.app.service.user.model.impl.DefaultGrantedAuthority;
import com.tbs.app.service.view.IViewService;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.enums.SystemRoleTypeEnum;
import com.tbs.shared.pojo.model.view.FormConfig;
import com.tbs.view.route.api.IViewRouteService;
import com.tbs.view.route.impl.DatabaseViewRouteServiceImpl;
import com.tbs.view.route.model.ViewRouteMetaModel;
import com.tbs.view.route.model.ViewRouteModel;
import com.tbs.view.route.model.ViewRouteVersionModel;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 视图控制器
 * <p>
 * 提供视图相关的接口，用于管理表单配置和视图路由
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/private/view")
@Tag(name = "视图控制器", description = "视图控制器")
@PreAuthorize("@authUtil.userLoggedIn")
public class ViewController extends BaseController {

    @Resource
    private IViewService viewService;

    @Resource
    private IViewRouteService viewRouteService;

    /**
     * 网关配置属性
     */
    @Resource
    private GatewayProperty gatewayProperty;

    /**
     * 根据key获取表单配置
     *
     * @param id 配置ID
     * @return 表单配置
     */
    @GetMapping("/form/{id}")
    @Operation(summary = "获取表单配置", description = "根据配置ID获取表单配置")
    @ApiResponse(responseCode = "200", description = "成功获取表单配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = FormConfig.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public FormConfig getForm(@Parameter(name = "id", description = "配置ID", required = true) @PathVariable Long id) {
        return viewService.getForm(id);
    }

    /**
     * 获取所有表单配置
     * <p>
     * 分页获取系统中所有的表单配置信息
     * </p>
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 所有表单配置列表
     */
    @GetMapping("/listForms")
    @Operation(summary = "获取所有表单配置", description = "获取所有表单配置")
    @ApiResponse(responseCode = "200", description = "成功获取所有表单配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public List<FormConfig> listForms(int pageNum, int pageSize) {
        return viewService.listForms(pageNum, pageSize);
    }

    /**
     * 保存表单配置
     *
     * @param formConfig 表单配置
     * @return 保存结果
     */
    @PostMapping("saveForm")
    @AjaxResponseType(FormConfig.class)
    @Operation(summary = "保存表单配置", description = "保存表单配置")
    @ApiResponse(responseCode = "200", description = "成功保存表单配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult saveForm(@Parameter(description = "表单配置") @RequestBody FormConfig formConfig) {
        return successResult(viewService.saveForm(formConfig));
    }

    /**
     * 删除表单配置
     *
     * @param id 表单配置ID
     * @return 删除结果
     */
    @DeleteMapping("/form/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除表单配置", description = "根据ID删除表单配置")
    @ApiResponse(responseCode = "200", description = "成功删除表单配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult deleteForm(
        @Parameter(name = "id", description = "配置ID", required = true) @PathVariable Long id) {
        return successResult(viewService.deleteForm(id));
    }

    /**
     * 更新表单配置
     *
     * @param formConfigId 表单配置ID
     * @param formConfig   表单配置
     * @return 更新结果
     */
    @PutMapping("updateForm/{id}")
    @AjaxResponseType(FormConfig.class)
    @Operation(summary = "更新表单配置", description = "更新表单配置")
    @ApiResponse(responseCode = "200", description = "成功更新表单配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult updateForm(
        @Parameter(description = "表单配置ID", required = true) @PathVariable("id") Long formConfigId,
        @Parameter(description = "表单配置") @RequestBody FormConfig formConfig) {
        return successResult(viewService.updateForm(formConfigId, formConfig));
    }

    /**
     * 获取所有视图路由配置
     * <p>
     * 获取系统中所有的视图路由配置信息，按组分类。该方法会根据当前用户的角色自动过滤无权限访问的路由。
     * 具体流程如下：
     * <ol>
     *     <li>获取当前用户的所有角色</li>
     *     <li>遍历所有视图路由组</li>
     *     <li>对每个组的路由根据用户角色进行过滤</li>
     *     <li>返回过滤后的所有路由配置
     * </ol>
     * </p>
     *
     * @return 包含所有视图路由配置的AjaxResult对象
     */
    @GetMapping("/routes")
    @AjaxResponseType(value = java.util.List.class, genericArgs = {ViewRouteModel.class})
    @Operation(summary = "获取所有视图路由配置", description = "获取所有视图路由配置")
    @ApiResponse(responseCode = "200", description = "成功获取所有视图路由配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult getViewRoutes() {
        var result = new LinkedList<ViewRouteModel>();
        // 适配新权限架构：从权限组中提取菜单权限编码用于路由过滤
        var menuPermissionCodes = extractMenuPermissionCodes();
        log.debug("当前用户菜单权限编码: {}", menuPermissionCodes);
        for (String group : CollUtil.isEmpty(gatewayProperty.getRouteGroups()) ? List.of("default")
            : gatewayProperty.getRouteGroups()) {
            var viewRoutes = viewRouteService.getViewRoutesByGroup(group);
            var filtered = removeWithRoleAndTranslate(menuPermissionCodes, viewRoutes);
            result.addAll(filtered);
        }
        return successResult(result);
    }

    /**
     * 从当前用户的权限组中提取菜单权限（type=1）的编码集合
     * <p>
     * 新权限架构下，用户关联的是权限组，每个权限组包含多个权限项。 菜单路由过滤应仅使用 type=MENU_ROLE 的权限项编码进行匹配。
     * </p>
     *
     * @return 菜单权限编码集合
     */
    private Set<String> extractMenuPermissionCodes() {
        var authorities = AuthUtil.getInstance().getCurrentUser().getAuthorities();
        if (CollUtil.isEmpty(authorities)) {
            return Set.of();
        }
        var result = new HashSet<String>();
        for (GrantedAuthority authority : authorities) {
            if (authority instanceof DefaultGrantedAuthority) {
                var grantedAuthority = (DefaultGrantedAuthority)authority;
                var roleGroup = grantedAuthority.getRoleGroup();
                if (roleGroup != null && CollUtil.isNotEmpty(roleGroup.getPermissions())) {
                    roleGroup.getPermissions().stream()
                        .filter(p -> Objects.equals(p.getType(), SystemRoleTypeEnum.MENU_ROLE.getCode()))
                        .forEach(p -> result.add(p.getRole()));
                }
            } else {
                // 兼容旧模式：直接使用 authority 字符串
                log.warn("使用旧模式权限：{}", authority.getAuthority());
                result.add(authority.getAuthority());
            }
        }
        return result;
    }


    /**
     * 根据用户角色过滤视图路由列表
     * <p>
     * 递归地过滤视图路由列表，移除用户没有权限访问的路由。过滤逻辑如下：
     * <ul>
     *     <li>如果用户角色集合为空，则返回空列表</li>
     *     <li>如果视图路由的meta信息为空，则保留该路由</li>
     *     <li>如果视图路由的meta信息中角色列表为空，则保留该路由</li>
     *     <li>如果用户角色与路由所需角色有交集，则保留该路由并递归过滤其子路由</li>
     *     <li>否则移除该路由</li>
     * </ul>
     * </p>
     *
     * @param roles      用户角色集合
     * @param viewRoutes 待过滤的视图路由列表
     * @return 过滤后的视图路由列表
     */
    List<ViewRouteModel> removeWithRoleAndTranslate(Set<String> roles, List<ViewRouteModel> viewRoutes) {
        if (CollUtil.isEmpty(viewRoutes)) {
            return List.of();
        }
        if (roles == null) {
            roles = Set.of();
        }
        List<ViewRouteModel> result = new ArrayList<>();
        for (ViewRouteModel viewRoute : viewRoutes) {
            // 深拷贝对象，避免翻译操作污染原始缓存数据
            ViewRouteModel copy = copyRouteModel(viewRoute);
            var flag = false;
            if (copy.getMeta() != null) {
                if (CollUtil.isNotEmpty(copy.getMeta().getRoles())) {
                    for (String role : copy.getMeta().getRoles()) {
                        if (roles.contains(role)) {
                            flag = true;
                            break;
                        }
                    }
                } else {
                    flag = true;
                }
            }
            if (flag) {
                // 角色校验通过后才进行翻译
                copy.setName(I18nUtil.get(copy.getName()));
                if (copy.getMeta() != null) {
                    copy.getMeta().setTitle(I18nUtil.get(copy.getMeta().getTitle()));
                }
                if (CollUtil.isNotEmpty(copy.getChildren())) {
                    copy.setChildren(removeWithRoleAndTranslate(roles, copy.getChildren().stream()
                        .collect(Collectors.toList())));
                }
                result.add(copy);
            }
        }
        return result;
    }

    /**
     * 深拷贝路由模型对象
     * <p>
     * 复制路由及其子路由、元数据的所有字段，确保翻译操作不会影响原始数据
     * </p>
     *
     * @param source 源路由模型
     * @return 深拷贝后的路由模型
     */
    private ViewRouteModel copyRouteModel(ViewRouteModel source) {
        ViewRouteModel copy = new ViewRouteModel();
        copy.setPath(source.getPath());
        copy.setComponent(source.getComponent());
        copy.setName(source.getName());
        copy.setRedirect(source.getRedirect());
        copy.setHidden(source.getHidden());
        copy.setAlwaysShow(source.getAlwaysShow());

        // 拷贝元数据
        if (source.getMeta() != null) {
            ViewRouteMetaModel metaCopy = new ViewRouteMetaModel();
            metaCopy.setRouteGroup(source.getMeta().getRouteGroup());
            metaCopy.setTitle(source.getMeta().getTitle());
            metaCopy.setIcon(source.getMeta().getIcon());
            metaCopy.setRoles(source.getMeta().getRoles() != null
                ? new java.util.ArrayList<>(source.getMeta().getRoles()) : null);
            metaCopy.setBreadcrumb(source.getMeta().getBreadcrumb());
            metaCopy.setActiveMenu(source.getMeta().getActiveMenu());
            copy.setMeta(metaCopy);
        }

        // 递归拷贝子路由
        if (CollUtil.isNotEmpty(source.getChildren())) {
            List<ViewRouteModel> childrenCopy = source.getChildren().stream()
                .map(this::copyRouteModel)
                .collect(Collectors.toList());
            copy.setChildren(childrenCopy);
        }

        return copy;
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
        return "视图控制器";
    }

    /**
     * 保存路由配置
     * <p>
     * 保存指定分组的路由配置信息，支持动态编辑路由。保存会产生一条新的版本记录，历史版本不可变。
     * </p>
     *
     * @param group           路由分组名称
     * @param description     版本说明，可为空
     * @param viewRouteModels 路由配置列表
     * @return 保存结果（含新产生的版本记录）
     */
    @PostMapping("/routes/{group}")
    @AjaxResponseType(ViewRouteVersionModel.class)
    @Operation(summary = "保存路由配置", description = "保存指定分组的路由配置，产生新版本记录")
    @ApiResponse(responseCode = "200", description = "成功保存路由配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult saveViewRoutes(
        @Parameter(name = "group", description = "路由分组名称", required = true) @PathVariable String group,
        @Parameter(name = "description", description = "版本说明") @RequestParam(required = false) String description,
        @Parameter(description = "路由配置列表") @RequestBody List<ViewRouteModel> viewRouteModels) {
        return successResult(viewRouteService.saveViewRoute(group, viewRouteModels, description));
    }

    /**
     * 删除路由配置
     * <p>
     * 删除指定分组的路由配置
     * </p>
     *
     * @param group 路由分组名称
     * @return 删除结果
     */
    @DeleteMapping("/routes/{group}")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除路由配置", description = "删除指定分组的路由配置")
    @ApiResponse(responseCode = "200", description = "成功删除路由配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult deleteViewRoutes(
        @Parameter(name = "group", description = "路由分组名称", required = true) @PathVariable String group) {
        viewRouteService.deleteViewRoute(group);
        return successResult(null);
    }

    /**
     * 获取所有路由分组
     * <p>
     * 获取系统中所有的路由分组名称
     * </p>
     *
     * @return 路由分组列表
     */
    @GetMapping("/route-groups")
    @AjaxResponseType(value = java.util.List.class, genericArgs = {String.class})
    @Operation(summary = "获取所有路由分组", description = "获取所有路由分组")
    @ApiResponse(responseCode = "200", description = "成功获取所有路由分组",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult getViewRouteGroups() {
        return successResult(viewRouteService.getAllViewRouteGroups());
    }

    /**
     * 获取指定分组的路由配置
     * <p>
     * 获取指定分组下的所有路由配置信息
     * </p>
     *
     * @param group 路由分组名称
     * @return 路由配置列表
     */
    @GetMapping("/routes/{group}")
    @AjaxResponseType(value = java.util.List.class, genericArgs = {ViewRouteModel.class})
    @Operation(summary = "获取指定分组的路由配置", description = "获取指定分组的路由配置")
    @ApiResponse(responseCode = "200", description = "成功获取指定分组的路由配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult getViewRoutesByGroup(
        @Parameter(name = "group", description = "路由分组名称", required = true) @PathVariable String group) {
        return successResult(viewRouteService.getViewRoutesByGroup(group));
    }

    /**
     * 刷新路由缓存
     * <p>
     * 强制刷新路由缓存，从数据库重新加载路由配置
     * </p>
     *
     * @return 刷新结果
     */
    @PostMapping("/routes/refresh")
    @AjaxResponseType(void.class)
    @Operation(summary = "刷新路由缓存", description = "强制刷新路由缓存")
    @ApiResponse(responseCode = "200", description = "成功刷新路由缓存",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult refreshRouteCache() {
        // 如果是DatabaseViewRouteServiceImpl，则刷新缓存
        if (viewRouteService instanceof DatabaseViewRouteServiceImpl) {
            ((DatabaseViewRouteServiceImpl)viewRouteService).refreshCache();
        }
        return successResult();
    }

    /**
     * 获取指定分组的版本历史
     * <p>
     * 按版本序号倒序返回该分组的全部版本记录，含版本说明、创建人与创建时间
     * </p>
     *
     * @param group 路由分组名称
     * @return 版本记录列表（最新版本在前）
     */
    @GetMapping("/routes/{group}/versions")
    @AjaxResponseType(value = java.util.List.class, genericArgs = {ViewRouteVersionModel.class})
    @Operation(summary = "获取分组版本历史", description = "按版本序号倒序返回该分组的版本记录")
    @ApiResponse(responseCode = "200", description = "成功获取版本历史",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult getRouteVersions(
        @Parameter(name = "group", description = "路由分组名称", required = true) @PathVariable String group) {
        return successResult(viewRouteService.getVersionHistory(group));
    }

    /**
     * 回滚到指定版本
     * <p>
     * 复制目标历史版本的路由数据并产生一条新的版本记录，历史依然不可变
     * </p>
     *
     * @param group     路由分组名称
     * @param versionId 目标版本ID
     * @return 回滚结果（含新产生的版本记录）
     */
    @PostMapping("/routes/{group}/rollback/{versionId}")
    @AjaxResponseType(ViewRouteVersionModel.class)
    @Operation(summary = "回滚到指定版本", description = "复制目标版本的路由数据并产生新版本记录")
    @ApiResponse(responseCode = "200", description = "成功回滚",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult rollbackRouteVersion(
        @Parameter(name = "group", description = "路由分组名称", required = true) @PathVariable String group,
        @Parameter(name = "versionId", description = "目标版本ID", required = true) @PathVariable Long versionId) {
        return successResult(viewRouteService.rollbackToVersion(group, versionId));
    }
}
