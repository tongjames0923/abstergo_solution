package com.tbs.app.controller.publics;

import com.tbs.app.form.LoginForm;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.access.IUsernamePasswordAccessTokenProvider;
import com.tbs.web.uitl.AjaxUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "公开用户控制器", description = "公开用户控制器")
@Slf4j
public class PublicUserController extends BaseController {

    @Resource
    IUsernamePasswordAccessTokenProvider loginProvider;

    @Resource
    HttpServletRequest request;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户使用用户名和密码登录系统")
    @ApiResponse(responseCode = "200", description = "成功登录", 
        content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ITokenModel.class)))
    public ITokenModel login(@Parameter(description = "登录表单") @RequestBody @Validated LoginForm loginForm) {
        ITokenModel tokenModel = loginProvider.requestToken(loginForm);
        return tokenModel;
    }
    
    @GetMapping("refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @ApiResponse(responseCode = "200", description = "成功刷新令牌", 
        content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = AjaxResult.class)))
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