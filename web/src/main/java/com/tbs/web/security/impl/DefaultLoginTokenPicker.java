package com.tbs.web.security.impl;

import com.tbs.web.config.property.WebAuthProperty;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

/**
 * 默认登录令牌拾取器
 * <p>
 * 基于 {@link WebAuthProperty} 配置初始化令牌的提取规则：字段名、来源（header/cookie/param）、
 * 过期时间。扩展自 {@link SimpleTokenPicker}，在容器启动后由 {@code @PostConstruct} 注入配置参数。
 * </p>
 *
 * @see SimpleTokenPicker 令牌拾取基类
 * @see WebAuthProperty 认证相关配置
 */
public class DefaultLoginTokenPicker extends SimpleTokenPicker {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultLoginTokenPicker.class);

    @Resource
    private WebAuthProperty webAuthProperty;

    @PostConstruct
    public void init() {
        // 从配置中心加载令牌提取策略，供请求时按字段名与来源解析令牌
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