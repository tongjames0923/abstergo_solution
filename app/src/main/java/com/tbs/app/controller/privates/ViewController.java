package com.tbs.app.controller.privates;


import com.tbs.app.service.view.IViewService;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.pojo.model.view.FormConfig;
import com.tbs.view.route.api.IViewRouteService;
import com.tbs.view.route.model.ViewRouteMetaModel;
import com.tbs.view.route.model.ViewRouteModel;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
        var authorities = AuthUtil.getInstance().getCurrentUser().getAuthorities();
        if (authorities == null) {
            authorities = new LinkedList<>();
        }
        var roles = authorities.stream().map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
        for (String group : viewRouteService.getAllViewRouteGroups()) {
            var viewRoutes = viewRouteService.getViewRoutesByGroup(group);
            var filtered = removeWithRoleAndTranslate(roles, viewRoutes);
            result.addAll(filtered);
        }
        return successResult(result);
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
        if (CollUtil.isEmpty(roles)) {
            return List.of();
        }
        if (CollUtil.isEmpty(viewRoutes)) {
            return List.of();
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
}
