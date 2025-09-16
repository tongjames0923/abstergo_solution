package com.tbs.web.security.impl

import com.tbs.common.error.AssertUtil
import com.tbs.web.security.authentication.center.IAuthenticationCenter
import com.tbs.web.security.authentication.model.ITokenAuthentication
import com.tbs.web.security.authentication.model.DefaultTokenAuthentication
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import java.util.function.Supplier
import javax.annotation.Resource

class TokenAuthenticationProvider : org.springframework.security.authentication.AuthenticationProvider {

    @Resource
    lateinit var authCenter: IAuthenticationCenter

    @Throws(AuthenticationException::class)
    public override fun authenticate(authentication: Authentication?): Authentication {
        AssertUtil.notNull(authentication, "未获取到有效的Authentication");
        val tokenAuthentication = authentication as ITokenAuthentication
        AssertUtil.isTrue(
            tokenAuthentication.getAuthException() != null,
            Supplier { tokenAuthentication.getAuthException() })

        return authCenter.authenticate(authentication);
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return authentication == DefaultTokenAuthentication::class.java
    }
}
