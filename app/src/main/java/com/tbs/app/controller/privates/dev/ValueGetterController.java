package com.tbs.app.controller.privates.dev;

import com.tbs.app.form.ValueGetterForm;
import com.tbs.app.vo.ValueGetterMetaVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.script.api.expressible.IValueGetter;
import com.tbs.script.api.expressible.IValueGetterService;
import com.tbs.script.api.expressible.vo.ResultValue;
import com.tbs.web.BaseController;
import com.tbs.web.exception.NotFoundTokenException;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * 值获取器管理控制器
 * <p>
 * 用于查询系统中所有值获取器的元信息（名称、参数定义、返回类型）， 以及在调试/编辑脚本时直接调用某个值获取器取回结果
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("private/value-getter")
@Tag(name = "值获取器管理接口", description = "值获取器查询与调用相关操作接口")
public class ValueGetterController extends BaseController {

    @Resource
    private IValueGetterService valueGetterService;

    /**
     * 获取值获取器列表
     *
     * @param nameLike 名称模糊匹配（可选）
     * @param idLike   ID 模糊匹配（可选）
     * @return 值获取器元信息列表
     */
    @GetMapping("list")
    @AjaxResponseType(value = java.util.List.class, genericArgs = {ValueGetterMetaVO.class})
    @Operation(summary = "获取值获取器列表", description = "获取系统中所有值获取器的元信息，可按名称或 ID 模糊过滤")
    @ApiResponse(responseCode = "200", description = "成功获取值获取器列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult list(@Parameter(name = "nameLike", description = "值获取器名称模糊匹配") @RequestParam(
            required = false) String nameLike,
        @Parameter(name = "idLike", description = "值获取器 ID 模糊匹配") @RequestParam(
            required = false) String idLike) {
        List<IValueGetter> getters = valueGetterService.getValueGetters(nameLike, idLike);
        List<ValueGetterMetaVO> result = getters.stream().map(this::toMeta).collect(Collectors.toList());
        return successResult(result);
    }

    /**
     * 获取指定值获取器的详细信息
     *
     * @param form 查询表单，包含值获取器 ID
     * @return 值获取器元信息
     */
    @PostMapping("detail")
    @AjaxResponseType(ValueGetterMetaVO.class)
    @Operation(summary = "获取值获取器详情", description = "根据值获取器 ID 获取其参数定义与返回类型定义")
    @ApiResponse(responseCode = "200", description = "成功获取值获取器详情",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult detail(@Parameter(name = "form", description = "值获取器查询表单",
        required = true) @RequestBody ValueGetterForm form) {
        AssertUtil.isTrue(form == null || StrUtil.isEmpty(form.getGetterId()),
            () -> new NotFoundTokenException("未找到指定的值获取器"));
        IValueGetter getter = valueGetterService.getValueGetter(form.getGetterId());
        AssertUtil.isTrue(getter == null, () -> new NotFoundTokenException("未找到指定的值获取器"));
        return successResult(toMeta(getter));
    }

    /**
     * 调用值获取器
     *
     * @param form 调用表单，包含值获取器 ID 与参数值
     * @return 值获取器返回结果
     */
    @PostMapping("invoke")
    @AjaxResponseType(ResultValue.class)
    @Operation(summary = "调用值获取器", description = "根据值获取器 ID 与参数值执行取值，返回结果值")
    @ApiResponse(responseCode = "200", description = "成功调用值获取器",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult invoke(@Parameter(name = "form", description = "值获取器调用表单",
        required = true) @RequestBody ValueGetterForm form) {
        AssertUtil.isTrue(form == null || StrUtil.isEmpty(form.getGetterId()),
            () -> new NotFoundTokenException("未找到指定的值获取器"));
        ResultValue resultValue = valueGetterService.invokeValueGetter(form.getGetterId(), form.getParamValues());
        return successResult(resultValue);
    }

    /**
     * 值获取器元信息映射
     */
    private ValueGetterMetaVO toMeta(IValueGetter getter) {
        return ValueGetterMetaVO.builder().getterId(getter.getGetterId()).getterName(getter.getGetterName())
            .description(getter.getDescription()).paramTypes(getter.requestParamTypes())
            .resultType(getter.responseResultType()).build();
    }

    /**
     * 获取业务名称
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "值获取器控制器";
    }
}
