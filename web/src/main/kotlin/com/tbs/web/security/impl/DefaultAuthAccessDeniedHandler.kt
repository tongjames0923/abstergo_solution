package com.tbs.web.security.impl

import com.tbs.web.result.AjaxResult
import com.tbs.web.security.authentication.model.ITokenAuthentication
import com.tbs.web.uitl.AjaxUtil
import com.tbs.web.uitl.AuthUtil
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import java.io.IOException
import java.util.function.Function
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class DefaultAuthAccessDeniedHandler : AccessDeniedHandler {
    private companion object

    val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(DefaultAuthAccessDeniedHandler::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun handle(
        request: HttpServletRequest?, response: HttpServletResponse?,
        accessDeniedException: AccessDeniedException
    ) {
        log.error("权限异常", accessDeniedException)
        val res = AuthUtil.getInstance().authentication
            .map<AjaxResult?>(Function { authentication: ITokenAuthentication ->
                AjaxUtil.getInstance().custom(
                    AjaxResult.DEFAULT_FORBIDDEN_CODE,
                    AjaxResult.DEFAULT_FORBIDDEN_MESSAGE,
                    authentication.authException
                )
            }).orElse(
                AjaxUtil.getInstance().custom(
                    AjaxResult.DEFAULT_FORBIDDEN_CODE,
                    "无权限访问", null
                )
            )
        AjaxUtil.writeInfoResp(response, res)
        return
    }
}
