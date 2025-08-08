package com.tbs.web.security.impl

import com.tbs.common.error.AssertUtil
import com.tbs.web.exception.NotFoundTokenException
import com.tbs.web.security.ITokenAuthentication
import com.tbs.web.security.ITokenManager
import com.tbs.web.security.IUserAuthService
import org.springframework.context.annotation.Lazy
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import java.util.function.Supplier
import javax.annotation.Resource

class TokenAuthenticationProvider : org.springframework.security.authentication.AuthenticationProvider {
    @Lazy
    @Resource
    private lateinit var userAuthService: IUserAuthService

    @Lazy
    @Resource
    private lateinit var tokenManager: ITokenManager

    @Throws(AuthenticationException::class)
    public override fun authenticate(authentication: Authentication?): Authentication {
        AssertUtil.notNull(authentication, "未获取到有效的Authentication");
        val tokenAuthentication = authentication as ITokenAuthentication
        AssertUtil.isTrue(
            tokenAuthentication.getAuthException() != null,
            Supplier { tokenAuthentication.getAuthException() })
        AssertUtil.isTrue(!tokenAuthentication.hasToken(), Supplier { NotFoundTokenException("未获取到有效的Token") })
        userAuthService.authToken(tokenAuthentication.getTokenModel())
        val principal = tokenManager.loadUserInfoByToken(tokenAuthentication.getTokenModel())
        return TokenAuthentication.createFinal(tokenAuthentication.getTokenModel(), principal)
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return authentication == TokenAuthentication::class.java
    }
}
