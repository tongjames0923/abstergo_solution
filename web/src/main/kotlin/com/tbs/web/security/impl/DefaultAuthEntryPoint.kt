package com.tbs.web.security.impl

import com.tbs.common.util.StrUtil
import com.tbs.web.exception.ReLoginRequestException
import com.tbs.web.result.AjaxResult
import com.tbs.web.uitl.AjaxUtil
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class DefaultAuthEntryPoint : AuthenticationEntryPoint {
    private companion object

    val log = org.slf4j.LoggerFactory.getLogger(DefaultAuthEntryPoint::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun commence(
        request: HttpServletRequest?, response: HttpServletResponse?,
        authException: AuthenticationException
    ) {
        log.error("认证异常", authException)
        if (authException is ReLoginRequestException) {
            AjaxUtil.writeInfoResp(
                response, AjaxUtil.getInstance().custom(
                    AjaxResult.RELOGIN_CODE,
                    if (StrUtil.isEmpty(authException.getLocalizedMessage()))
                        AjaxResult.DEFAULT_RELOGIN_MESSAGE
                    else
                        authException.getLocalizedMessage(), authException
                )
            )
        }
        AjaxUtil.writeInfoResp(
            response, AjaxUtil.getInstance()
                .custom(AjaxResult.DEFAULT_UNAUTHORIZED_CODE, authException.getLocalizedMessage(), authException)
        )
    }
}
