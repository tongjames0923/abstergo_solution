package com.tbs.app.controller.privates;

import com.tbs.app.form.OssDownloadCodeForm;
import com.tbs.app.form.OssDownloadCodeSearchForm;
import com.tbs.app.service.oss.downloadcode.IOssDownloadCodeService;
import com.tbs.app.vo.OssDownloadCodeStatisticsVO;
import com.tbs.common.data.PageVO;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.oss.downloadcode.OssDownloadCodeInfo;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * OSS公开下载码管理控制器
 * <p>
 * 提供下载唯一码的生成、查询、更新与删除能力，位于 {@code /private/**} 路径下需认证访问；
 * 凭码下载文件的公开接口见 {@code PublicOssDownloadCodeController}。
 * 控制器只做HTTP接入与参数组装，业务校验由{@link IOssDownloadCodeService}实现承担
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("private/oss/download-code")
@Tag(name = "OSS公开下载码管理接口", description = "下载唯一码的生成、查询、更新与删除，需认证访问")
public class OssDownloadCodeController extends BaseController {

    @Resource
    private IOssDownloadCodeService ossDownloadCodeService;

    /**
     * 生成下载唯一码
     * <p>
     * 为指定OSS对象生成下载码，默认自动生成16位Base64随机码；
     * form.code非空时使用自定义码（须为4~64位Base64字符且唯一），创建时校验对象存在性；
     * expireSeconds为空或小于等于0表示永不过期
     * </p>
     *
     * @param form 创建表单（objectKey必填，code/bucket/expireSeconds/remark可选）
     * @return 创建成功的下载码信息
     */
    @PostMapping
    @AjaxResponseType(OssDownloadCodeInfo.class)
    @Operation(summary = "生成下载唯一码", description = "为指定存储对象生成下载码，可自定义4~64位Base64码、指定有效期与备注")
    @ApiResponse(responseCode = "200", description = "成功生成下载唯一码",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult create(@Parameter(name = "form", description = "下载码创建表单",
        required = true) @RequestBody OssDownloadCodeForm form) {
        return successResult(
            ossDownloadCodeService.createCode(form.getCode(), form.getObjectKey(), form.getBucket(),
                form.getExpireSeconds(), form.getRemark()));
    }

    /**
     * 查询下载唯一码信息
     *
     * @param code 下载唯一码
     * @return 下载码信息
     */
    @GetMapping
    @AjaxResponseType(OssDownloadCodeInfo.class)
    @Operation(summary = "查询下载唯一码", description = "按唯一码查询下载码的详细信息")
    @ApiResponse(responseCode = "200", description = "成功查询下载唯一码",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult get(@Parameter(name = "code", description = "下载唯一码",
        required = true) @RequestParam("code") String code) {
        return successResult(ossDownloadCodeService.getCode(code));
    }

    /**
     * 更新下载唯一码
     * <p>
     * 支持续期与修改备注，表单中未传的字段不修改
     * </p>
     *
     * @param code 下载唯一码
     * @param form 更新表单（expireSeconds/remark可选）
     * @return 空结果
     */
    @PutMapping
    @AjaxResponseType(void.class)
    @Operation(summary = "更新下载唯一码", description = "按唯一码更新有效期（续期/改为永不过期）与备注")
    @ApiResponse(responseCode = "200", description = "成功更新下载唯一码",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult update(@Parameter(name = "code", description = "下载唯一码",
        required = true) @RequestParam("code") String code,
        @Parameter(name = "form", description = "下载码更新表单",
            required = true) @RequestBody OssDownloadCodeForm form) {
        ossDownloadCodeService.updateCode(code, form.getExpireSeconds(), form.getRemark());
        return successResult();
    }

    /**
     * 删除下载唯一码
     *
     * @param code 下载唯一码
     * @return 空结果
     */
    @DeleteMapping
    @AjaxResponseType(void.class)
    @Operation(summary = "删除下载唯一码", description = "按唯一码删除下载码，删除后公开下载接口立即失效")
    @ApiResponse(responseCode = "200", description = "成功删除下载唯一码",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult delete(@Parameter(name = "code", description = "下载唯一码",
        required = true) @RequestParam("code") String code) {
        ossDownloadCodeService.deleteCode(code);
        return successResult();
    }

    /**
     * 分页查询下载码
     * <p>
     * 支持按码精确匹配、对象键前缀、关键字与状态筛选，结果按创建时间倒序
     * </p>
     *
     * @param form 查询表单（条件与分页参数，均可选）
     * @return 分页结果（总数与当前页数据）
     */
    @PostMapping("page")
    @AjaxResponseType(value = PageVO.class, genericArgs = {OssDownloadCodeInfo.class})
    @Operation(summary = "分页查询下载码", description = "按条件分页查询下载码列表，支持关键字、对象键与状态筛选")
    @ApiResponse(responseCode = "200", description = "成功分页查询下载码",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult queryPage(@Parameter(name = "form", description = "下载码查询表单") @RequestBody(
        required = false) OssDownloadCodeSearchForm form) {
        return successResult(ossDownloadCodeService.queryPage(form));
    }

    /**
     * 统计下载码概览
     *
     * @return 统计结果（总数/有效/已过期/永久有效）
     */
    @GetMapping("statistics")
    @AjaxResponseType(OssDownloadCodeStatisticsVO.class)
    @Operation(summary = "统计下载码概览", description = "统计下载码总数、有效数、已过期数与永久有效数")
    @ApiResponse(responseCode = "200", description = "成功统计下载码概览",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult statistics() {
        return successResult(ossDownloadCodeService.statistics());
    }

    /**
     * 获取业务名称
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "OSS公开下载码管理";
    }
}
