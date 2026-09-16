package com.tbs.app.controller.privates;

import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.app.form.ExternalIdentityBindForm;
import com.tbs.app.service.identity.ExternalUserProfile;
import com.tbs.app.service.identity.IExternalIdentityExchanger;
import com.tbs.app.service.identity.IUserExternalIdentityService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.pojo.entity.SystemUserExternalIdentityEntity;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 外部身份绑定控制器
 * <p>
 * 提供外部身份绑定的通用管理能力：当前用户绑定查询、登录态自助绑定（平台凭证换取后绑定本人）、
 * 管理端分页查询、手工绑定与解绑；平台差异经 {@code IExternalIdentityExchanger} 收敛，
 * 本控制器不感知具体平台。自助类接口仅允许操作当前登录用户本人。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private/external-identity")
@Tag(name = "外部身份绑定控制器", description = "外部身份绑定查询、自助绑定与管理")
@Slf4j
public class PrivateExternalIdentityController extends BaseController {

    @Resource
    private IUserExternalIdentityService externalIdentityService;

    @Resource
    private ISystemUserService systemUserService;

    private final Map<String, IExternalIdentityExchanger> exchangerMap;

    /**
     * 构建控制器并按身份类型索引平台凭证换取实现
     *
     * @param exchangers 容器中全部平台凭证换取实现，可为空
     */
    public PrivateExternalIdentityController(List<IExternalIdentityExchanger> exchangers) {
        this.exchangerMap = exchangers == null ? new HashMap<>()
            : exchangers.stream()
                .collect(Collectors.toMap(IExternalIdentityExchanger::getIdentityType, Function.identity(), (a, b) -> a));
    }

    /**
     * 查询当前用户的全部外部身份绑定
     *
     * @return 绑定列表
     */
    @GetMapping("/my")
    @AjaxResponseType(SystemUserExternalIdentityEntity.class)
    @PreAuthorize("@preAuthUtils.accountActived")
    @Operation(summary = "我的外部身份", description = "查询当前登录用户的全部外部身份绑定")
    @ApiResponse(responseCode = "200", description = "成功查询",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult my() {
        return successResult(externalIdentityService.queryByUserId(resolveCurrentUserId()));
    }

    /**
     * 自助绑定当前用户
     * <p>
     * 以平台凭证（如企微授权码）换取外部身份后绑定到当前登录用户本人，
     * 与登录链路共用同一换取实现；仅允许绑定本人。
     * </p>
     *
     * @param identityType 外部身份类型（如 wecom）
     * @param credential   平台凭证键值（如 { code: 企微授权码 }）
     * @return 操作结果
     */
    @PostMapping("/self-bind/{identityType}")
    @AjaxResponseType(void.class)
    @PreAuthorize("@preAuthUtils.accountActived")
    @Operation(summary = "自助绑定", description = "以平台凭证换取外部身份并绑定到当前登录用户本人")
    @ApiResponse(responseCode = "200", description = "成功绑定",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult selfBind(
        @Parameter(description = "外部身份类型，如 wecom") @PathVariable("identityType") String identityType,
        @Parameter(description = "平台凭证键值，字段由各平台契约约定") @RequestBody Map<String, Object> credential) {
        IExternalIdentityExchanger exchanger = exchangerMap.get(identityType);
        AssertUtil.isTrue(exchanger == null).i18n(SystemI18nConstant.EXTERNAL_IDENTITY_TYPE_UNSUPPORTED)
            .params(identityType).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        ExternalUserProfile profile = exchanger.exchange(credential);
        AssertUtil.isTrue(profile == null || StrUtil.isEmpty(profile.getExternalId()))
            .i18n(SystemI18nConstant.EXTERNAL_IDENTITY_PARAM_REQUIRED).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        externalIdentityService.bind(identityType, profile.getExternalId(), resolveCurrentUserId());
        return successResult();
    }

    /**
     * 分页查询绑定关系（管理端）
     *
     * @param identityType 外部身份类型过滤，可选
     * @param userName     系统登录用户名过滤，可选
     * @param pageNum      页码，从 1 起
     * @param pageSize     每页数量
     * @return 分页结果
     */
    @GetMapping("/page")
    @AjaxResponseType(SystemUserExternalIdentityEntity.class)
    @PreAuthorize("@preAuthUtils.accountActived")
    @Operation(summary = "绑定分页查询", description = "按外部身份类型与系统用户名过滤分页查询绑定关系")
    @ApiResponse(responseCode = "200", description = "成功查询",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult page(
        @Parameter(description = "外部身份类型过滤") @RequestParam(value = "identityType", required = false) String identityType,
        @Parameter(description = "系统登录用户名过滤") @RequestParam(value = "userName", required = false) String userName,
        @Parameter(description = "页码，从 1 起") @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
        @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return successResult(externalIdentityService.queryPage(identityType, userName, pageNum, pageSize));
    }

    /**
     * 手工绑定（管理端）
     *
     * @param form 绑定表单（外部身份类型、外部身份标识、系统登录用户名）
     * @return 操作结果
     */
    @PostMapping("/bind")
    @AjaxResponseType(void.class)
    @PreAuthorize("@preAuthUtils.accountActived")
    @Operation(summary = "手工绑定", description = "按登录用户名与外部身份标识建立绑定关系")
    @ApiResponse(responseCode = "200", description = "成功绑定",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult bind(@Parameter(description = "绑定表单") @RequestBody ExternalIdentityBindForm form) {
        AssertUtil.isTrue(form == null || StrUtil.isEmpty(form.getIdentityType())
            || StrUtil.isEmpty(form.getExternalId()) || StrUtil.isEmpty(form.getUserName()))
            .i18n(SystemI18nConstant.EXTERNAL_IDENTITY_PARAM_REQUIRED).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        Long userId = resolveUserIdByUserName(form.getUserName());
        externalIdentityService.bind(form.getIdentityType(), form.getExternalId(), userId);
        return successResult();
    }

    /**
     * 解除绑定（管理端）
     *
     * @param identityType 外部身份类型
     * @param userId       系统用户 ID
     * @return 操作结果
     */
    @DeleteMapping("/unbind/{identityType}/{userId}")
    @AjaxResponseType(void.class)
    @PreAuthorize("@preAuthUtils.accountActived")
    @Operation(summary = "解除绑定", description = "解除指定用户在指定平台的外部身份绑定")
    @ApiResponse(responseCode = "200", description = "成功解绑",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult unbind(
        @Parameter(description = "外部身份类型") @PathVariable("identityType") String identityType,
        @Parameter(description = "系统用户 ID") @PathVariable("userId") Long userId) {
        externalIdentityService.unbind(identityType, userId);
        return successResult();
    }

    /**
     * 解析当前登录用户的系统用户 ID
     *
     * @return 当前用户 ID
     */
    private Long resolveCurrentUserId() {
        var userDetails = AuthUtil.getInstance().getCurrentUser();
        return resolveUserIdByUserName(userDetails.getUsername());
    }

    /**
     * 按登录用户名解析系统用户 ID
     *
     * @param userName 登录用户名
     * @return 系统用户 ID
     */
    private Long resolveUserIdByUserName(String userName) {
        var users = systemUserService.queryByUserName(userName);
        AssertUtil.isTrue(CollUtil.isEmpty(users)).i18n(SystemI18nConstant.USER_NOT_FOUND).params(userName)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        return users.get(0).getId();
    }

    /**
     * 获取当前控制器的业务名称
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "外部身份绑定控制器";
    }
}
