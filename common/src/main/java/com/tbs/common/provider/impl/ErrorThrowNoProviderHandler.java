package com.tbs.common.provider.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.provider.INoProviderHandler;
import org.springframework.stereotype.Component;

/**
 * 无服务提供者时抛出系统级异常的处理器。
 */
@Component
public class ErrorThrowNoProviderHandler implements INoProviderHandler {
    @Override
    public void handle(Class<?> clazz, String method, Object[] args) {
        AssertUtil.isTrueSystemError(true).i18n("provider.no.provider.found", clazz, method).throwIt();
    }
}
