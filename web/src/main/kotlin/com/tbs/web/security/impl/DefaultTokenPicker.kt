package com.tbs.web.security.impl

import cn.hutool.core.date.DateField
import cn.hutool.core.date.DateUtil
import com.alibaba.fastjson2.JSON
import com.tbs.common.error.AssertUtil
import com.tbs.common.util.StrUtil
import com.tbs.web.config.property.WebAuthProperty
import com.tbs.web.enums.TokenSource
import com.tbs.web.exception.NotFoundTokenException
import com.tbs.web.security.ITokenModel
import com.tbs.web.security.ITokenPicker
import com.tbs.web.security.model.SimpleTokenModel
import java.util.*
import java.util.function.Supplier
import javax.annotation.Resource
import javax.servlet.http.HttpServletRequest

class DefaultTokenPicker : ITokenPicker {
    @Resource
    private lateinit var webAuthProperty: WebAuthProperty

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
    override fun pick(request: HttpServletRequest): ITokenModel? {
        var token: String? = ""
        val sourceField: String?
        var sourceValue: String? = ""
        sourceField = webAuthProperty.getTokenFiledName()
        when (webAuthProperty.getTokenSource()) {
            TokenSource.BODY -> {
                val body = getBody(request)
                AssertUtil.isTrue(
                    StrUtil.isEmpty(sourceValue),
                    { return@isTrue NotFoundTokenException("未找到Token") })
                val map = JSON.parseObject(body, Map::class.java)
                token = map.get(webAuthProperty.getTokenFiledName()).toString()
                sourceValue = body
            }

            TokenSource.COOKIE -> {
                sourceValue = getFromCookie(request, webAuthProperty.getTokenFiledName())
                AssertUtil.isTrue(
                    StrUtil.isEmpty(sourceValue),
                    { return@isTrue NotFoundTokenException("未找到Token") })
                token = sourceValue
            }

            TokenSource.HEADER -> {
                sourceValue = request.getHeader(webAuthProperty.getTokenFiledName())
                AssertUtil.isTrue(
                    StrUtil.isEmpty(sourceValue),
                    { return@isTrue NotFoundTokenException("未找到Token") })
                token = sourceValue
            }

            TokenSource.PARAM -> {
                sourceValue = getFromParam(request, webAuthProperty.getTokenFiledName())
                AssertUtil.isTrue(
                    StrUtil.isEmpty(sourceValue),
                    { return@isTrue NotFoundTokenException("未找到Token") })
                token = sourceValue
            }

            else -> AssertUtil.throwError<NotFoundTokenException?>(Supplier { NotFoundTokenException("未找到Token") })
        }
        val createDate = Date()
        return SimpleTokenModel.create(
            token, webAuthProperty.getTokenSource(), sourceField, sourceValue,
            DateUtil.offset(createDate, DateField.SECOND, webAuthProperty.getTokenExpireTime())
        )
    }
}
