package com.tbs.app.controller.publics;

import com.tbs.app.form.LoginForm;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.access.IUsernamePasswordAccessTokenProvider;
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
    IUsernamePasswordAccessTokenProvider loginProvider;

    @Resource
    HttpServletRequest request;

    @PostMapping("/login")
    public ITokenModel login(@RequestBody @Validated LoginForm loginForm) {
        ITokenModel tokenModel = loginProvider.requestToken(loginForm);
        return tokenModel;
    }
    @GetMapping("refresh")
    public AjaxResult refresh() {
        var refreshTokenService = SpringUtil.getBeanOptional(IRefreshTokenService.class);
        if (refreshTokenService.isEmpty()) {
            return AjaxUtil.getInstance().custom(AjaxResult.REFRESH_FAIL_CODE, "不支持无感刷新", null);
        }
        var service = refreshTokenService.get();
        try {
            ITokenModel newModel = service.useRefreshToken();
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
