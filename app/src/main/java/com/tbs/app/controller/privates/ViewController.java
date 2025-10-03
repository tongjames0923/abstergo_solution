package com.tbs.app.controller.privates;

import com.tbs.app.service.view.IViewService;
import com.tbs.shared.pojo.model.view.FormConfig;
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

@RestController
@RequestMapping("/private/view")
@Tag(name = "视图控制器", description = "视图控制器")
public class ViewController extends BaseController {

    @Resource
    private IViewService viewService;

    /**
     * 根据key获取表单配置
     *
     * @param id 配置ID
     * @return 表单配置
     */
    @GetMapping("/form/{id}")
    @Operation(summary = "获取表单配置", description = "根据配置ID获取表单配置")
    @ApiResponse(responseCode = "200", description = "成功获取表单配置", 
        content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = FormConfig.class)))
    public FormConfig getForm(@Parameter(name = "id", description = "配置ID", required = true) @PathVariable Long id) {
        return viewService.getForm(id);
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
        content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult saveForm(@Parameter(description = "表单配置") @RequestBody FormConfig formConfig) {
        return successResult(viewService.saveForm(formConfig));
    }

    @Override
    public String getBusinessName() {
        return "视图控制器";
    }
}