package com.tbs.app.controller.privates;

import com.tbs.web.BaseController;
import com.tbs.web.security.IUserAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/private/user")
public class PrivateUserController extends BaseController {
    @Resource
    private IUserAuthService userAuthService;

    @GetMapping("/logout")
    public Boolean logout() {
        return userAuthService.logout(getCurrentTokenModel());
    }
}
