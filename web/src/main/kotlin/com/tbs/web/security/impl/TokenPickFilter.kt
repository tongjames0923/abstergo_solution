package com.tbs.web.security.impl

import com.tbs.web.exception.NotFoundTokenException
import com.tbs.web.security.ITokenPicker
import com.tbs.web.uitl.AuthUtil
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.annotation.Resource
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class TokenPickFilter : OncePerRequestFilter() {


    private companion object {
        val log = org.slf4j.LoggerFactory.getLogger(TokenPickFilter::class.java)
    }

    @Lazy
    @Resource
    private val tokenPicker: ITokenPicker? = null
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        try {
            val tokenModel = tokenPicker?.pick(request)
            if (tokenModel != null) {
                AuthUtil.getInstance().setAuthentication(TokenAuthentication.create(tokenModel));
            }
        } catch (e: NotFoundTokenException) {
            log.trace("Not found token")
        }

        filterChain.doFilter(request, response)
    }
}