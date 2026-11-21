package com.tbs.app.controller.privates;

import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.app.service.event.EventReportSupport;
import com.tbs.app.service.event.reporter.BusinessEventReporter;
import com.tbs.app.service.role.ISystemRoleGroupService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.user.model.ISystemUserDetails;
import com.tbs.app.vo.DepartmentInfoVO;
import com.tbs.app.vo.UserInfoVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.web.BaseController;
import com.tbs.web.constant.WebI18nConstant;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.token.ITokenRemover;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.uitl.AjaxUtil;
import com.tbs.web.uitl.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 登录后用户相关控制器
 * <p>
 * 该控制器用于处理已登录用户的相关操作，包括用户登出、获取用户信息、获取用户状态选项等
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/private/user")
@PreAuthorize("@authUtil.userLoggedIn")
@Tag(name = "登录后用户接口", description = "登录后用户接口")
public class PrivateUserController extends BaseController {

    @Resource
    private ISystemRoleGroupService groupService;

    @Resource
    private ISystemUserService userService;

    @Resource
    ITokenRemover tokenRemover;

    @Resource
    IUserLoginInfoService userLoginInfoService;

    @Resource
    IDepartmentUserRelationService departmentUserRelationService;

    @Resource
    ISystemDepartmentService departmentService;

    @Resource
    EventReportSupport eventReportSupport;

    /**
     * 用户登出
     *
     * @return 登出结果，true表示登出成功
     */
    @GetMapping("/logout")
    @AjaxResponseType(void.class)
    @Operation(summary = "用户登出", description = "退出当前登录状态")
    @ApiResponse(responseCode = "200", description = "成功登出",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Boolean.class)))
    public AjaxResult logout() {
        String logoutUserName = resolveCurrentUserName();
        try {
            ITokenModel tokenModel = AuthUtil.getInstance().getCurrentTokenModel();
            tokenRemover.inactiveToken(tokenModel);
            userLoginInfoService.removeUserLoginInfo(tokenModel.getToken());
        } catch (Exception e) {
            // 不得把异常对象放入响应 data（会被序列化成完整堆栈），也不得直接回传内部异常消息
            log.warn("用户登出失败，令牌失效处理异常", e);
            return AjaxUtil.getInstance().error(AjaxUtil.resolveErrorMessage(e), null);
        }

        try {
            var refreshTokenService = SpringUtil.getBeanOptional(IRefreshTokenService.class);
            refreshTokenService.ifPresent(service -> service.inactiveRefreshToken());

        } catch (Exception e) {
            log.warn("刷新Token失效失败 {}", e.getMessage());
        }
        reportLogoutEvent(logoutUserName);
        return AjaxUtil.getInstance().success(null);
    }

    /**
     * 解析当前登录用户名，解析失败返回 null。
     *
     * <p>登出流程会失效令牌，因此必须在失效前读取用户名。</p>
     *
     * @return 当前用户名；未登录或解析异常时返回 null
     */
    private String resolveCurrentUserName() {
        try {
            var user = AuthUtil.getInstance().getCurrentUser();
            return user == null ? null : user.getUsername();
        } catch (Exception e) {
            log.debug("登出埋点解析当前用户失败, 原因={}", e.getMessage());
            return null;
        }
    }

    /**
     * 上报用户登出事件，异常由上报门面吞掉。
     *
     * @param userName 登出用户名
     */
    private void reportLogoutEvent(String userName) {
        Long userId = null;
        if (userName != null) {
            try {
                userId = userService.queryByUserName(userName).stream().findFirst().map(SystemUserEntity::getId)
                    .orElse(null);
            } catch (Exception e) {
                log.debug("登出埋点解析用户标识失败, userName={}, 原因={}", userName, e.getMessage());
            }
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(BusinessEventReporter.PARAM_USER_ID, userId);
        params.put(BusinessEventReporter.PARAM_USER_NAME, userName);
        eventReportSupport.reportQuietly(BusinessEventReporter.REPORTER_ID, BusinessEventReporter.EVENT_USER_LOGGED_OUT,
            params);
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息VO对象，包含用户名、昵称、头像和可用权限列表
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息，包括用户名、昵称、头像和可用权限列表")
    @ApiResponse(responseCode = "200", description = "成功获取用户信息",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfoVO.class)))
    public UserInfoVO info() {
        UserInfoVO userInfoVO = new UserInfoVO();
        var userDetail = AuthUtil.getInstance().getCurrentUser();
        userInfoVO.setUserName(userDetail.getUsername());
        Optional<SystemUserEntity> user = userService.queryByUserName(userDetail.getUsername()).stream().findFirst();
        AssertUtil.isTrue(!user.isPresent()).message("用户{} 不存在", userDetail.getUsername())
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        userInfoVO.setId(user.get().getId());
        userInfoVO.setNickName(user.get().getNickName());
        userInfoVO.setAccountStatus(user.get().getFlag());
        userInfoVO.setAccountStatusStr(EnumUtil.valueOf(SystemUserStatusEnum.class, user.get().getFlag()).getValue());
        userInfoVO.setAvatar(user.get().getAvatar());
        userInfoVO.setAvaliableRoles(groupService.getUserRoleGroupVOList(user.get().getId()));
        if (userDetail instanceof ISystemUserDetails) {
            userInfoVO.setCurrentDepartment(((ISystemUserDetails)userDetail).getCurrentDepartment());
        }

        userInfoVO.setAvaliableDepartments(DepartmentInfoVO.convertToDepartmentVOs(
            departmentUserRelationService.queryUserDepartments(user.get().getId())));
        return userInfoVO;
    }

    /**
     * 获取刷新令牌
     *
     * @return 刷新令牌
     */
    @GetMapping("getRefreshToken")
    @AjaxResponseType(void.class)
    @Operation(summary = "获取刷新令牌", description = "获取用于无感刷新的令牌")
    @ApiResponse(responseCode = "200", description = "成功获取刷新令牌",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult getRefreshToken() {
        var refreshTokenService = SpringUtil.getBeanOptional(IRefreshTokenService.class);
        if (refreshTokenService.isEmpty()) {
            return AjaxUtil.getInstance().success("不支持无感刷新", null);
        }
        refreshTokenService.get().createRefreshToken();
        return successResult(null);
    }

    @PostMapping("setCurrentDepartment/{departmentId}")
    @AjaxResponseType(void.class)
    @Operation(summary = "设置当前部门", description = "设置当前登录用户所选部门")
    @ApiResponse(responseCode = "200", description = "成功设置当前部门",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult setCurrentDepartment(@PathVariable Long departmentId) {
        var auth = AuthUtil.getInstance().getAuthentication();
        AssertUtil.isTrue(auth.isEmpty()).i18n(WebI18nConstant.AUTH_INFO_EMPTY)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        // 部门归属校验与持久化统一在服务层完成，控制器只负责取当前令牌并委托
        userLoginInfoService.changeCurrentDepartment(auth.get().getTokenModel(), departmentId);
        return successResult(null);
    }


    //    /**
    //     * 获取用户状态选项
    //     *
    //     * @return 用户状态选项集合
    //     */
    //    @GetMapping("/statusOptions")
    //    @Cacheable(cacheNames = CacheNamesConstants.DEMO_DEAD_CACHE, key = "'userStatusOptions'")
    //    @Operation(summary = "获取用户状态选项", description = "获取用户状态的下拉选项列表，结果会被缓存")
    //    @ApiResponse(responseCode = "200", description = "成功获取用户状态选项",
    //        content = @Content(mediaType = "application/json",
    //            schema = @Schema(implementation = Collection.class)))
    //    public Collection<OptionValue<Integer>> getUserStatusOptions() {
    //        return Arrays.stream(SystemUserStatusEnum.values()).map(m -> {
    //            return new OptionValue<>(m.getCode(), m.getValue());
    //        }).collect(Collectors.toSet());
    //    }

    @Override
    public String getBusinessName() {
        return "登录后基础用户业务";
    }
}
