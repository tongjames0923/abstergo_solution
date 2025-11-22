package com.tbs.app.controller.privates;

import com.tbs.app.service.view.IViewService;
import com.tbs.shared.pojo.model.view.FormConfig;
import com.tbs.view.route.api.IViewRouteService;
import com.tbs.view.route.model.ViewRouteModel;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/private/view")
@Tag(name = "视图控制器", description = "视图控制器")
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
    public FormConfig getForm(@Parameter(name = "id", description = "配置ID", required = true) @PathVariable Long id) {
        return viewService.getForm(id);
    }

    /**
     * 获取所有表单配置
     *
     * @return 所有表单配置
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
    @Operation(summary = "保存表单配置", description = "保存表单配置")
    @ApiResponse(responseCode = "200", description = "成功保存表单配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
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
    @Operation(summary = "删除表单配置", description = "根据ID删除表单配置")
    @ApiResponse(responseCode = "200", description = "成功删除表单配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult deleteForm(
        @Parameter(name = "id", description = "配置ID", required = true) @PathVariable Long id) {
        // TODO: 实现删除逻辑
        return successResult("功能尚未实现");
    }

    /**
     * 更新表单配置
     *
     * @param formConfigId 表单配置ID
     * @param formConfig   表单配置
     * @return 更新结果
     */
    @PutMapping("updateForm/{id}")
    @Operation(summary = "更新表单配置", description = "更新表单配置")
    @ApiResponse(responseCode = "200", description = "成功更新表单配置",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult updateForm(
        @Parameter(description = "表单配置ID", required = true) @PathVariable("id") Long formConfigId,
        @Parameter(description = "表单配置") @RequestBody FormConfig formConfig) {
        return successResult(viewService.updateForm(formConfigId, formConfig));
    }

    /**
     * 获取所有视图路由配置
     *
     * @return 路由配置信息
     */
    @GetMapping("/routes")
    public AjaxResult getViewRoutes() {
        var result = new LinkedList<ViewRouteModel>();
        for (String group : viewRouteService.getAllViewRouteGroups()) {
            result.addAll(viewRouteService.getViewRoutesByGroup(group));
        }
        return successResult(result);
    }

    @Override
    public String getBusinessName() {
        return "视图控制器";
    }
}
