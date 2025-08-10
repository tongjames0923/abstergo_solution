package com.tbs.app.controller.publics;

import com.tbs.app.form.LoginForm;
import com.tbs.web.BaseController;
import com.tbs.web.security.ITokenModel;
import com.tbs.web.security.IUserAuthService;
import io.swagger.annotations.Api;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

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

    @Override
    public String getBusinessName() {
        return "公开用户控制器";
    }

}
