package com.tbs.app.controller.publics;

import com.tbs.app.form.AppVersionCheckForm;
import com.tbs.app.service.version.IAppVersionCompatibilityPolicyService;
import com.tbs.app.vo.AppVersionUpdateCheckVO;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 公开应用版本升级检查控制器
 * <p>
 * 面向客户端应用（Android/iOS/Windows/macOS/Linux）的升级检查接口，
 * 位于 {@code /public/**} 路径下，无需管理员认证即可访问。
 * <p>
 * 客户端上报自身的应用标识、平台与当前内部版本号，
 * 服务端综合最新已发布版本与生效的兼容策略，返回是否需要升级、
 * 升级动作强度以及最新版本安装包下载地址（OSS 预签名 URL 或外部托管链接）
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/public/app-update")
@Tag(name = "应用升级检查公开接口", description = "面向客户端应用的升级检查，无需认证即可调用")
public class PublicAppVersionUpdateController extends BaseController {

    @Resource
    private IAppVersionCompatibilityPolicyService policyService;

    /**
     * 客户端升级检查
     * <p>
     * 客户端上报自身应用标识、平台与当前版本号，服务端返回是否需要升级及最新版本信息（含下载地址）
     *
     * @param form 升级检查入参（appKey、platform、currentVersionCode）
     * @return 升级检查结果，包含 needUpdate、action、最新版本信息与 downloadUrl
     */
    @PostMapping("check")
    @AjaxResponseType(AppVersionUpdateCheckVO.class)
    @Operation(summary = "升级检查", description = "客户端上报版本信息，检查是否需要升级并获取下载地址")
    @ApiResponse(responseCode = "200", description = "成功执行升级检查",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult checkUpgrade(@Parameter(name = "form", description = "升级检查入参",
        required = true) @RequestBody AppVersionCheckForm form) {
        return successResult(
            policyService.evaluateCompatibility(form.getAppKey(), form.getPlatform(), form.getCurrentVersionCode()));
    }

    @Override
    public String getBusinessName() {
        return "应用升级检查";
    }
}
