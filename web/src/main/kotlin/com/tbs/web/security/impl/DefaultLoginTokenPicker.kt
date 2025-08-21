package com.tbs.web.security.impl

import com.tbs.web.config.property.WebAuthProperty
import javax.annotation.PostConstruct
import javax.annotation.Resource


/**
 * 默认登录TokenPicker
 */
open class DefaultLoginTokenPicker : SimpleTokenPicker() {
    companion object {
        val log = org.slf4j.LoggerFactory.getLogger(DefaultLoginTokenPicker::class.java)
    }

    @Resource
    private lateinit var webAuthProperty: WebAuthProperty

    @PostConstruct
    fun init() {
        fieldName = webAuthProperty.tokenFiledName
        tokenSource = webAuthProperty.tokenSource
        expireTime = webAuthProperty.tokenExpireTime
        log.info(
            "DefaultLoginTokenPicker init: fieldName={}, tokenSource={}, expireTime={}",
            fieldName,
            tokenSource,
            expireTime
        )
    }
}
