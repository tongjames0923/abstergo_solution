package com.tbs.app.controller.privates;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tbs.app.service.business.role.IUserRoleRelationService;
import com.tbs.app.service.business.user.ISystemUserService;
import com.tbs.app.vo.UserInfoVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.view.vo.OptionValue;
import com.tbs.web.BaseController;
import com.tbs.web.security.user.IUserAuthService;
import com.tbs.web.uitl.AuthUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 登录后用户相关控制器
 * <p>
 * 该控制器用于处理已登录用户的相关操作，包括用户登出、获取用户信息、获取用户状态选项等
 * </p>
 *
 * @author tongj
 */
@RestController
@RequestMapping("/private/user")
@PreAuthorize("@authUtil.userLoggedIn")
@Api(value = "登录后用户接口", tags = "登录后用户接口")
public class PrivateUserController extends BaseController {
	@Resource
	private IUserAuthService userAuthService;

	@Resource
	private IUserRoleRelationService relationService;

	@Resource
	private ISystemUserService userService;

	/**
	 * 用户登出
	 *
	 * @return 登出结果，true表示登出成功
	 */
	@GetMapping("/logout")
	@ApiOperation(value = "用户登出", notes = "退出当前登录状态")
	public Boolean logout() {
		return userAuthService.logout(AuthUtil.getInstance().getCurrentTokenModel());
	}

	/**
	 * 获取当前登录用户信息
	 *
	 * @return 用户信息VO对象，包含用户名、昵称、头像和可用角色列表
	 */
	@GetMapping("/info")
	@ApiOperation(value = "获取用户信息", notes = "获取当前登录用户的详细信息，包括用户名、昵称、头像和可用角色列表")
	public UserInfoVO info() {
		UserInfoVO userInfoVO = new UserInfoVO();
		userInfoVO.setUserName(AuthUtil.getInstance().getCurrentUser().getUsername());
		Optional<SystemUserEntity> user = userService
				.queryByUserName(AuthUtil.getInstance().getCurrentUser().getUsername()).stream().findFirst();
		AssertUtil.isTrue(!user.isPresent(),
				() -> new FormattedRuntimeException("用户{} 不存在", AuthUtil.getInstance().getCurrentUser().getUsername()));
		userInfoVO.setNickName(user.get().getNickName());
		userInfoVO.setAvatar(user.get().getAvatar());
		userInfoVO.setAvaliableRoles(relationService.queryUserRoles(user.get().getId()).stream()
				.map(SystemRoleEntity::getRole).collect(Collectors.toList()));
		return userInfoVO;
	}

	/**
	 * 获取用户状态选项
	 *
	 * @return 用户状态选项集合
	 */
	@GetMapping("/statusOptions")
	@Cacheable(cacheNames = "DEAD_CACHE", key = "'userStatusOptions'")
	@ApiOperation(value = "获取用户状态选项", notes = "获取用户状态的下拉选项列表，结果会被缓存")
	public Collection<OptionValue<Integer>> getUserStatusOptions() {
		return Arrays.stream(SystemUserStatusEnum.values()).map(m -> {
			return new OptionValue<>(m.getCode(), m.getValue());
		}).collect(Collectors.toSet());
	}

	@Override
	public String getBusinessName() {
		return "登录后基础用户业务";
	}
}
