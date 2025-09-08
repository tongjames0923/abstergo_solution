package com.tbs.app.controller.publics;

import com.tbs.app.form.LoginForm;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserAuthService;
import com.tbs.web.uitl.AjaxUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 公开的用户控制器
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/public/user")
@Api(value = "公开用户控制器")
@Slf4j
public class PublicUserController extends BaseController {

    @Resource
    IUserAuthService authService;

    @Resource
    IRefreshTokenService refreshTokenService;

    @Resource
    HttpServletRequest request;

    @PostMapping("/login")
    public ITokenModel login(@RequestBody @Validated LoginForm loginForm) {
        return authService.login(loginForm.getUsername(), loginForm.getPassword());
    }

    @GetMapping("refresh")
    public AjaxResult refresh() {
        try {
            ITokenModel newModel = refreshTokenService.refreshAccessToken(request);
            return successResult(newModel);
        } catch (Exception e) {
            return AjaxUtil.getInstance().custom(AjaxResult.REFRESH_FAIL_CODE, e.getLocalizedMessage(), null);
        }
    }

    @Override
    public String getBusinessName() {
        return "公开用户控制器";
    }

}
