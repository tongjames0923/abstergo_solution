package com.tbs.web.security.impl

import com.alibaba.fastjson2.JSON
import com.tbs.web.enums.TokenSource
import com.tbs.web.exception.NotFoundTokenException
import com.tbs.web.gate.ITokenPicker
import javax.servlet.http.HttpServletRequest

open class SimpleTokenPicker : ITokenPicker {

    var fieldName: String = "";
    var tokenSource: TokenSource = TokenSource.HEADER

    var expireTime: Int = 0

    private fun getBody(request: HttpServletRequest): String? {
        val sb = StringBuilder()
        try {
            request.getReader().use { reader ->
                reader.lines().forEach { str: String? -> sb.append(str) }
            }
            return sb.toString()
        } catch (e: Exception) {
            return null
        }
    }

    private fun getFromCookie(request: HttpServletRequest, name: String?): String? {
        val cookies = request.getCookies()
        if (cookies == null) {
            return null
        }
        for (cookie in cookies) {
            if (cookie.getName() == name) {
                return cookie.getValue()
            }
        }
        return null
    }

    private fun getFromParam(request: HttpServletRequest, name: String?): String? {
        return request.getParameter(name)
    }

    @Throws(NotFoundTokenException::class)
    override fun pick(request: HttpServletRequest): String? {
        var token: String? = ""


        when (tokenSource) {
            TokenSource.BODY -> {
                val body = getBody(request)

                val map = JSON.parseObject(body, Map::class.java)
                return map.get(fieldName).toString()

            }

            TokenSource.COOKIE -> {
                return getFromCookie(request, fieldName)

            }

            TokenSource.HEADER -> {
                return request.getHeader(fieldName)

            }

            TokenSource.PARAM -> {
                return getFromParam(request, fieldName)

            }

        }
       
    }
}
