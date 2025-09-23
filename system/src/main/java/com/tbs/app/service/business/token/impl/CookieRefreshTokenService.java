package com.tbs.app.service.business.token.impl;

import com.tbs.common.IStartup;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.security.token.AbstractRefreshTokenService;
import com.tbs.web.security.token.IRefreshTokenValidator;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.form.IRefreshTokenForm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 默认的Cookie刷新令牌服务实现
 *
 * @author abstergo
 */
@Slf4j
public class CookieRefreshTokenService extends AbstractRefreshTokenService implements IStartup {

    private Collection<IRefreshTokenValidator> refreshTokenValidators;

    @Resource
    private HttpServletResponse response;

    @Resource
    private HttpServletRequest request;

    @Override
    protected void pushRefreshToken(ITokenModel refreshToken) {
        Cookie cookie = new Cookie("REFRESH_TOKEN", refreshToken.getToken());
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @Override
    protected IRefreshTokenForm createRefreshTokenForm(@NotNull ITokenModel refreshToken) {
        return new SimpleRefreshTokenForm(refreshToken);
    }

    @Override
    protected Collection<IRefreshTokenValidator> getRefreshTokenValidators() {
        return refreshTokenValidators;
    }

    @Override
    protected Optional<String> getRefreshToken() {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("REFRESH_TOKEN".equals(cookie.getName())) {
                    return Optional.ofNullable(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void startUp() throws RuntimeException {
        this.refreshTokenValidators = SpringUtil.getBeansOfType(IRefreshTokenValidator.class).values().stream()
            .sorted(Comparator.comparingInt(IRefreshTokenValidator::getOrder)).collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    private static class SimpleRefreshTokenForm implements IRefreshTokenForm {
        private ITokenModel refreshToken;
    }
}
