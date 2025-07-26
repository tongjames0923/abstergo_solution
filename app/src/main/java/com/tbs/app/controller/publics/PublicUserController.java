package com.tbs.app.controller.publics;

import com.tbs.app.form.LoginForm;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.ITokenModel;
import com.tbs.web.security.IUserAuthService;
import com.tbs.web.uitl.AjaxUtil;
import io.swagger.annotations.Api;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

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

    @PostMapping("/login")
    public ITokenModel login(@RequestBody @Validated LoginForm loginForm) {
        return userAuthService.login(loginForm.getUsername(), loginForm.getPassword());
    }

    @GetMapping("/logout")
    public Boolean logout() {
        return userAuthService.logout(getCurrentTokenModel());
    }
}
