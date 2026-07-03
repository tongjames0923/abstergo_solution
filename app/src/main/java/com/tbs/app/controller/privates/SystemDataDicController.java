package com.tbs.app.controller.privates;

import com.tbs.app.form.DicItemSaveForm;
import com.tbs.app.form.DicItemSearchForm;
import com.tbs.app.form.DicSaveForm;
import com.tbs.app.form.DicSearchForm;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.dic.entity.SystemDataDicEntity;
import com.tbs.dic.entity.SystemDataDicItemEntity;
import com.tbs.dic.model.DicModel;
import com.tbs.dic.service.IDataDicService;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.constant.RoleConstants;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

/**
 * 数据字典管理控制器
 * <p>
 * 该控制器用于管理系统数据字典，提供字典和字典项的查询、新增、启用、禁用、删除等操作
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@PreAuthorize("@authUtil.userLoggedIn and hasAnyAuthority(" + "'" +
    RoleConstants.SUPER_ADMIN_ROLE_NAME + "', '" +
    RoleConstants.DEVELOPER_ROLE_NAME + "')")
@RequestMapping("private/admin/dic")
@Tag(name = "数据字典管理接口", description = "数据字典管理相关操作接口")
public class SystemDataDicController extends BaseController {

    /**
     * 数据字典服务
     */
    @DubboReference(lazy = true)
    private IDataDicService dataDicService;

    /**
     * 根据字典名称模糊查询字典列表
     * <p>
     * 先根据字典名称模糊匹配获取字典编码列表，再按字典编码过滤后查询完整字典信息
     * </p>
     *
     * @param form 查询条件，包含字典编码和字典名称
     * @return 匹配的字典模型列表
     */
    @PostMapping("query")
    @AjaxResponseType(value = List.class, genericArgs = {DicModel.class})
    @Operation(summary = "模糊查询字典", description = "根据字典名称和编码模糊查询字典列表")
    @ApiResponse(responseCode = "200", description = "成功查询字典列表",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult listDicByDicNameLike(
        @Parameter(name = "form", description = "字典查询条件") @RequestBody DicSearchForm form) {
        if (form == null) {
            form = new DicSearchForm();
        }
        if (form.getDicCode() == null) {
            form.setDicCode("");
        }
        if (form.getDicName() == null) {
            form.setDicName("");
        }
        var dicCodes = dataDicService.getDicCodesByNamesLike(form.getDicName());
        if (CollUtil.isEmpty(dicCodes)) {
            return successResult(CollUtil.newArrayList());
        }
        List<DicModel> dicModels = new LinkedList<>();
        for (var dicCode : dicCodes) {
            if (dicCode.contains(form.getDicCode())) {
                dicModels.addAll(dataDicService.getDicByCode(dicCode, true));
            }
        }
        return successResult(dicModels);
    }

    /**
     * 启用数据字典
     *
     * @param form 包含字典编码的查询表单
     * @return 操作结果
     */
    @PostMapping("enableDic")
    @AjaxResponseType(void.class)
    @Operation(summary = "启用数据字典", description = "根据字典编码启用对应的数据字典")
    @ApiResponse(responseCode = "200", description = "成功启用数据字典",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult enableDic(
        @Parameter(name = "form", description = "字典编码信息", required = true)
        @RequestBody DicSearchForm form) {
        AssertUtil.isTrue(form == null).message("字典编号不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getDicCode())).message("字典编号不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        dataDicService.enableDataDic(form.getDicCode());
        return successResult(null);
    }

    /**
     * 禁用数据字典
     *
     * @param form 包含字典编码的查询表单
     * @return 操作结果
     */
    @PostMapping("disableDic")
    @AjaxResponseType(void.class)
    @Operation(summary = "禁用数据字典", description = "根据字典编码禁用对应的数据字典")
    @ApiResponse(responseCode = "200", description = "成功禁用数据字典",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult disableDic(
        @Parameter(name = "form", description = "字典编码信息", required = true)
        @RequestBody DicSearchForm form) {
        AssertUtil.isTrue(form == null).message("字典编号不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getDicCode())).message("字典编号不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        dataDicService.disableDataDic(form.getDicCode());
        return successResult(null);
    }

    /**
     * 启用数据字典项
     *
     * @param form 包含字典编码和字典项标签的查询表单
     * @return 操作结果
     */
    @PostMapping("enableDicItem")
    @AjaxResponseType(void.class)
    @Operation(summary = "启用数据字典项", description = "根据字典编码和字典项标签启用对应的字典项")
    @ApiResponse(responseCode = "200", description = "成功启用数据字典项",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult enableDicItem(
        @Parameter(name = "form", description = "字典项查询条件", required = true)
        @RequestBody DicItemSearchForm form) {
        AssertUtil.isTrue(form == null).message("字典项不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getDicCode())).message("字典编号不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getLabel())).message("字典项不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        dataDicService.enableDataDicItem(form.getDicCode(), form.getLabel());
        return successResult(null);
    }

    /**
     * 禁用数据字典项
     *
     * @param form 包含字典编码和字典项标签的查询表单
     * @return 操作结果
     */
    @PostMapping("disableDicItem")
    @AjaxResponseType(void.class)
    @Operation(summary = "禁用数据字典项", description = "根据字典编码和字典项标签禁用对应的字典项")
    @ApiResponse(responseCode = "200", description = "成功禁用数据字典项",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult disableDicItem(
        @Parameter(name = "form", description = "字典项查询条件", required = true)
        @RequestBody DicItemSearchForm form) {
        AssertUtil.isTrue(form == null).message("字典项不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getDicCode())).message("字典编号不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getLabel())).message("字典项不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        dataDicService.disableDataDicItem(form.getDicCode(), form.getLabel());
        return successResult(null);
    }

    /**
     * 删除数据字典
     *
     * @param form 包含字典编码的查询表单
     * @return 操作结果
     */
    @PostMapping("deleteDic")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除数据字典", description = "根据字典编码删除对应的数据字典")
    @ApiResponse(responseCode = "200", description = "成功删除数据字典",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult deleteDic(
        @Parameter(name = "form", description = "字典编码信息", required = true)
        @RequestBody DicSearchForm form) {
        AssertUtil.isTrue(form == null).message("字典编号不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getDicCode())).message("字典编号不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        dataDicService.deleteDataDic(form.getDicCode());
        return successResult(null);
    }

    /**
     * 删除数据字典项
     *
     * @param form 包含字典编码和字典项标签的查询表单
     * @return 操作结果
     */
    @PostMapping("deleteDicItem")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除数据字典项", description = "根据字典编码和字典项标签删除对应的字典项")
    @ApiResponse(responseCode = "200", description = "成功删除数据字典项",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult deleteDicItem(
        @Parameter(name = "form", description = "字典项查询条件", required = true)
        @RequestBody DicItemSearchForm form) {
        AssertUtil.isTrue(form == null).message("字典项不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getDicCode())).message("字典编号不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getLabel())).message("字典项不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        dataDicService.deleteDataDicItem(form.getDicCode(), form.getLabel());
        return successResult(null);
    }

    /**
     * 保存数据字典
     * <p>
     * 根据表单数据创建数据字典实体并持久化，如果字典编码已存在则进行更新
     * </p>
     *
     * @param form 字典保存表单，包含字典编码和字典名称
     * @return 保存后的字典实体信息
     */
    @PostMapping("saveDic")
    @AjaxResponseType(SystemDataDicEntity.class)
    @Operation(summary = "保存数据字典", description = "创建或更新数据字典信息")
    @ApiResponse(responseCode = "200", description = "成功保存数据字典",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult saveDic(
        @Parameter(name = "form", description = "字典保存信息", required = true)
        @RequestBody DicSaveForm form) {
        AssertUtil.isTrue(form == null).message("字典不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getDicCode())).message("字典编号不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getDicName())).message("字典名称不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        SystemDataDicEntity dic = new SystemDataDicEntity();
        dic.setDataDicCode(form.getDicCode());
        dic.setDataDicName(form.getDicName());
        if (StrUtil.isNotBlank(form.getDicDesc())) {
            dic.setRemark(form.getDicDesc());
        }
        return successResult(dataDicService.saveDataDic(dic));
    }

    /**
     * 保存数据字典项
     * <p>
     * 根据表单数据创建数据字典项实体并持久化到指定字典下
     * </p>
     *
     * @param form 字典项保存表单，包含字典编码、字典项标签和字典项值
     * @return 操作结果
     */
    @PostMapping("saveDicItem")
    @AjaxResponseType(void.class)
    @Operation(summary = "保存数据字典项", description = "在指定字典下创建或更新字典项")
    @ApiResponse(responseCode = "200", description = "成功保存数据字典项",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult saveDicItem(
        @Parameter(name = "form", description = "字典项保存信息", required = true)
        @RequestBody DicItemSaveForm form) {
        AssertUtil.isTrue(form == null).message("字典项不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getDicCode())).message("字典编号不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getDicItemLabel())).message("字典项不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getDicItemValue())).message("字典项值不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        SystemDataDicItemEntity item = new SystemDataDicItemEntity();
        item.setDicCode(form.getDicCode());
        item.setLabel(form.getDicItemLabel());
        item.setValue(form.getDicItemValue());
        if (StrUtil.isNotBlank(form.getDicItemDesc())) {
            item.setRemark(form.getDicItemDesc());
        }
        dataDicService.saveDataDicItem(form.getDicCode(), item);
        return successResult(null);
    }


    @Override
    public String getBusinessName() {
        return "数据字典";
    }
}
