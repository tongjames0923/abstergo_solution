package com.tbs.app.controller.privates;

import com.tbs.app.constant.CommonConstant;
import com.tbs.app.pojo.entity.SystemUser;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.web.uitl.AuthUtil;
import com.tbs.app.vo.UserInfoVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.web.BaseController;
import com.tbs.web.security.IUserAuthService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Optional;

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
    private ISystemUserService userService;

    @GetMapping("/logout")
    public Boolean logout() {
        return userAuthService.logout(AuthUtil.getInstance().getCurrentTokenModel());
    }

    @GetMapping("/info")
    public UserInfoVO info() {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserName(AuthUtil.getInstance().getCurrentUser().getUsername());
        Optional<SystemUser> user =
            userService.queryByUserName(AuthUtil.getInstance().getCurrentUser().getUsername()).stream().findFirst();
        AssertUtil.isTrue(!user.isPresent(), () -> new FormattedRuntimeException("用户{} 不存在",
            AuthUtil.getInstance().getCurrentUser().getUsername()));
        userInfoVO.setNickName(user.get().getNickName());
        userInfoVO.setAvatar(user.get().getAvatar());
        return userInfoVO;
    }
}
