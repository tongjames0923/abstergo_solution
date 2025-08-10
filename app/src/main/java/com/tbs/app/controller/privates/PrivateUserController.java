package com.tbs.app.controller.privates;

import com.tbs.app.constant.CommonConstant;
import com.tbs.app.enums.SystemUserStatusEnum;
import com.tbs.app.pojo.entity.SystemRoleEntity;
import com.tbs.app.pojo.entity.SystemUserEntity;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.app.service.business.IUserRoleRelationService;
import com.tbs.app.vo.UserInfoVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.view.vo.OptionValue;
import com.tbs.web.BaseController;
import com.tbs.web.security.IUserAuthService;
import com.tbs.web.uitl.AuthUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author tongj
 */
@RestController
@RequestMapping("/private/user")
@PreAuthorize("@authUtil.userLoggedIn")
public class PrivateUserController extends BaseController {
    @Resource
    private IUserAuthService userAuthService;

    @Resource
    private IUserRoleRelationService relationService;

    @Resource
    private ISystemUserService userService;

    @GetMapping("/logout")
    public Boolean logout() {
        return userAuthService.logout(AuthUtil.getInstance().getCurrentTokenModel());
    }

    @GetMapping("/info")
    public UserInfoVO info() {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserName(AuthUtil.getInstance().getCurrentUser().getUsername());
        Optional<SystemUserEntity> user =
            userService.queryByUserName(AuthUtil.getInstance().getCurrentUser().getUsername()).stream().findFirst();
        AssertUtil.isTrue(!user.isPresent(), () -> new FormattedRuntimeException("用户{} 不存在",
            AuthUtil.getInstance().getCurrentUser().getUsername()));
        userInfoVO.setNickName(user.get().getNickName());
        userInfoVO.setAvatar(user.get().getAvatar());
        userInfoVO.setAvaliableRoles(relationService.queryUserRoles(user.get().getId()).stream()
            .filter(role -> role != null && CommonConstant.FLAG_ON.equals(role.getFlag()))
            .map(SystemRoleEntity::getRole).collect(Collectors.toList()));
        return userInfoVO;
    }

    /**
     * 获取用户状态选项
     *
     * @return
     */
    @GetMapping("/statusOptions")
    @Cacheable(cacheNames = "DEAD_CACHE", key = "'userStatusOptions'")
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
