package com.tbs.web.security.impl;

import com.tbs.web.config.property.WebAuthProperty;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 默认登录TokenPicker
 */
public class DefaultLoginTokenPicker extends SimpleTokenPicker {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultLoginTokenPicker.class);

    @Resource
    private WebAuthProperty webAuthProperty;

    @PostConstruct
    public void init() {
        fieldName = webAuthProperty.getTokenFiledName();
        tokenSource = webAuthProperty.getTokenSource();
        expireTime = webAuthProperty.getTokenExpireTime();
        log.info(
                "DefaultLoginTokenPicker init: fieldName={}, tokenSource={}, expireTime={}",
                fieldName,
                tokenSource,
                expireTime
        );
    }
}