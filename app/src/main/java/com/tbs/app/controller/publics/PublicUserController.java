package com.tbs.app.controller.publics;

import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.IUserAuthService;
import com.tbs.web.uitl.AjaxUtil;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 公开的用户控制器
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/public/user")
@Api(value = "公开用户控制器")
public class PublicUserController extends BaseController {

    @Resource
    IUserAuthService userAuthService;

    @RequestMapping("/login")
    public AjaxResult login(String username, String password) {
        return AjaxUtil.getInstance().success(userAuthService.login(username, password));
    }

    @RequestMapping("/logout")
    public AjaxResult logout() {
        return AjaxUtil.getInstance().success(userAuthService.logout(getCurrentTokenModel()));
    }
}
