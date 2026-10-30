package com.tbs.app.error.transformer;

import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.common.error.IErrorTransformer;
import com.tbs.common.exception.FormattedRuntimeException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * 访问被拒异常转换器
 * <p>
 * 框架异常原文（如 "Access Denied"）既无信息量也不支持多语言，统一转为 i18n 提示。
 * </p>
 *
 * @author abstergo
 */
@Component
public class AccessDeniedTransformer implements IErrorTransformer {
    @Override
    public boolean support(Throwable throwable) {
        return throwable != null && throwable instanceof AccessDeniedException;
    }

    @Override
    public FormattedRuntimeException toFormattedRuntimeException(Throwable throwable) {
        return FormattedRuntimeException.businessErrorI18n(SystemI18nConstant.AUTH_USER_ACCESS_DENIED);
    }

    /**
     * 必须优先于默认转换器（默认 support 恒为 true），否则本转换器永远不会命中
     *
     * @return 最高优先级
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
