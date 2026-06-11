package com.tbs.app.controller.privates;

import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.app.service.role.IUserRoleRelationService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.user.model.ISystemUserDetails;
import com.tbs.app.vo.DepartmentInfoVO;
import com.tbs.app.vo.UserInfoVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.web.BaseController;
import com.tbs.web.exception.NoLoginException;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 登录后用户相关控制器
 * <p>
 * 该控制器用于处理已登录用户的相关操作，包括用户登出、获取用户信息、获取用户状态选项等
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private/user")
@PreAuthorize("@authUtil.userLoggedIn")
@Tag(name = "登录后用户接口", description = "登录后用户接口")
public class PrivateUserController extends BaseController {

    @Resource
    private IUserRoleRelationService relationService;

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

    /**
     * 用户登出
     *
     * @return 登出结果，true表示登出成功
     */
    @GetMapping("/logout")
    @Operation(summary = "用户登出", description = "退出当前登录状态")
    @ApiResponse(responseCode = "200", description = "成功登出",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Boolean.class)))
    public AjaxResult logout() {

        try {
            ITokenModel tokenModel = AuthUtil.getInstance().getCurrentTokenModel();
            tokenRemover.inactiveToken(tokenModel);
            userLoginInfoService.removeUserLoginInfo(tokenModel.getToken());
        } catch (Exception e) {
            return AjaxUtil.getInstance().error(e.getMessage(), e);
        }

        try {
            var refreshTokenService = SpringUtil.getBeanOptional(IRefreshTokenService.class);
            refreshTokenService.ifPresent(service -> service.inactiveRefreshToken());

        } catch (Exception e) {
            return AjaxUtil.getInstance().debugError(e.getMessage(), e);
        }
        return AjaxUtil.getInstance().success(null);
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息VO对象，包含用户名、昵称、头像和可用角色列表
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息，包括用户名、昵称、头像和可用角色列表")
    @ApiResponse(responseCode = "200", description = "成功获取用户信息",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UserInfoVO.class)))
    public UserInfoVO info() {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserName(AuthUtil.getInstance().getCurrentUser().getUsername());
        Optional<SystemUserEntity> user =
            userService.queryByUserName(AuthUtil.getInstance().getCurrentUser().getUsername()).stream().findFirst();
        AssertUtil.isTrue(!user.isPresent(), () -> new FormattedRuntimeException("用户{} 不存在",
            AuthUtil.getInstance().getCurrentUser().getUsername()));
        userInfoVO.setNickName(user.get().getNickName());
        userInfoVO.setAvatar(user.get().getAvatar());
        userInfoVO.setAvaliableRoles(relationService.queryRoles(user.get().getId()));
        var ud = AuthUtil.getInstance().getCurrentUser();

        if (ud instanceof ISystemUserDetails) {
            userInfoVO.setCurrentDepartment(((ISystemUserDetails)ud).getCurrentDepartment());
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
    @Operation(summary = "设置当前部门", description = "设置当前登录用户所选部门")
    @ApiResponse(responseCode = "200", description = "成功设置当前部门",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult setCurrentDepartment(@PathVariable Long departmentId) {
        var auth = AuthUtil.getInstance().getAuthentication();
        AssertUtil.isTrue(auth.isEmpty(), () -> new NoLoginException("登录信息不存在"));
        var authentication = auth.get();
        var userDetails = authentication.getUserDetails();
        AssertUtil.isTrue(userDetails == null, () -> new NoLoginException("用户信息不存在"));
        AssertUtil.isTrue(!(userDetails instanceof ISystemUserDetails),
            () -> new FormattedRuntimeException("用户{}的用户信息数据不兼容，请联系后端开发查看用户登录信息配置。",
                userDetails.getUsername()));

        var ud = (ISystemUserDetails)userDetails;
        AssertUtil.isTrue(CollUtil.isEmpty(ud.getDepartments()), () -> {
            return new FormattedRuntimeException("用户{}未设置部门", ud.getUsername());
        });
        AssertUtil.isTrue(
            !ud.getDepartments().stream().map(DepartmentInfoVO::getDepartmentId).collect(Collectors.toSet())
                .contains(departmentId), () -> {
                return new FormattedRuntimeException("用户{}未加入部门{}", ud.getUsername(), departmentId);
            });
        ((ISystemUserDetails)userDetails).setCurrentDepartment(departmentId);
        userLoginInfoService.replaceUserDetails(ud, authentication.getTokenModel());
        return successResult(null);

    }


    //    /**
    //     * 获取用户状态选项
    //     *
    //     * @return 用户状态选项集合
    //     */
    //    @GetMapping("/statusOptions")
    //    @Cacheable(cacheNames = "DEAD_CACHE", key = "'userStatusOptions'")
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
