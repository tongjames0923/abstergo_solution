package com.tbs.web.security.impl

import cn.hutool.core.date.DateField
import cn.hutool.core.date.DateUtil
import com.alibaba.fastjson2.JSON
import com.tbs.common.util.StrUtil
import com.tbs.web.enums.TokenSource
import com.tbs.web.exception.NotFoundTokenException
import com.tbs.web.security.token.ITokenModel
import com.tbs.web.security.token.ITokenPicker
import com.tbs.web.security.token.model.SimpleTokenModel
import java.util.*
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
    override fun pick(request: HttpServletRequest): Optional<ITokenModel> {
        var token: String? = ""
        val sourceField: String?
        var sourceValue: String? = ""
        sourceField = fieldName
        when (tokenSource) {
            TokenSource.BODY -> {
                val body = getBody(request)
                if (StrUtil.isEmpty(sourceValue)) {
                    return Optional.empty()
                }
                val map = JSON.parseObject(body, Map::class.java)
                token = map.get(fieldName).toString()
                sourceValue = body
            }

            TokenSource.COOKIE -> {
                sourceValue = getFromCookie(request, fieldName)
                if (StrUtil.isEmpty(sourceValue)) {
                    return Optional.empty()
                }
                token = sourceValue
            }

            TokenSource.HEADER -> {
                sourceValue = request.getHeader(fieldName)
                if (StrUtil.isEmpty(sourceValue)) {
                    return Optional.empty()
                }
                token = sourceValue
            }

            TokenSource.PARAM -> {
                sourceValue = getFromParam(request, fieldName)
                if (StrUtil.isEmpty(sourceValue)) {
                    return Optional.empty()
                }
                token = sourceValue
            }

        }
        val createDate = Date()
        return Optional.of(
            SimpleTokenModel.create(
                token, tokenSource, sourceField, sourceValue,
                DateUtil.offset(createDate, DateField.SECOND, expireTime)
            )
        )
    }
}
