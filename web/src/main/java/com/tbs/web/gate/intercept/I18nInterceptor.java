package com.tbs.web.gate.intercept;

import com.tbs.common.data.ValueWrapper;
import com.tbs.common.i18n.I18nContextHolder;
import com.tbs.common.util.base.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * 国际化语言拦截器
 * <p>
 * 从HTTP请求中解析客户端语言偏好，设置到I18nContextHolder中。
 * <p>
 * 注意：此拦截器仅作为备份，主要的语言设置由 {@link com.tbs.web.gate.I18nFilter} 负责。
 * Filter在Spring Security之前执行，能确保登录流程也能正确获取Locale。
 * <p>
 * 语言解析优先级（从高到低）： 1. 请求参数 ?lang=zh_CN 或 ?locale=en_US 2. 请求头 Accept-Language 3. 请求头 X-Lang 或 X-Locale 4. 默认语言（zh_CN）
 *
 * @author abstergo
 * @see IApiInvokeInterceptor API调用拦截器接口
 * @see I18nContextHolder 国际化上下文持有者
 * @see com.tbs.web.gate.I18nFilter
 */
@Slf4j
@Component
public class I18nInterceptor implements IApiInvokeInterceptor {

    private static final String LANG_PARAM = "lang";
    private static final String LOCALE_PARAM = "locale";
    private static final String X_LANG_HEADER = "X-Lang";
    private static final String X_LOCALE_HEADER = "X-Locale";
    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

    
    @Override
    public ApiInvokeContext beforeInvoke(ApiInvokeContext context) {
        // 如果Filter已经设置了Locale，则不需要再次设置
        if (I18nContextHolder.hasLocale()) {
            return context;
        }

        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return context;
            }

            Locale locale = resolveLocale(request);
            if (locale != null) {
                I18nContextHolder.setLocale(locale);
                log.debug("I18nInterceptor设置请求语言: {}", locale);
            }
        } catch (Exception e) {
            log.warn("I18nInterceptor解析请求语言失败", e);
        }
        return context;
    }

    @Override
    public void afterInvoke(ApiInvokeContext context, ValueWrapper<Object> result) {
        // 不在这里清理，由 RequestContextLifecycleFilter 的 IContextClean 清理管道统一清理
    }

    @Override
    public void onException(ApiInvokeContext context, Throwable e) {
        // 不在这里清理，由 RequestContextLifecycleFilter 的 IContextClean 清理管道统一清理
    }

    protected Locale resolveLocale( HttpServletRequest request) {
        Locale locale = resolveFromParams(request);
        if (locale != null) {
            return locale;
        }

        locale = resolveFromAcceptLanguage(request);
        if (locale != null) {
            return locale;
        }

        locale = resolveFromCustomHeaders(request);
        if (locale != null) {
            return locale;
        }

        return null;
    }

    protected Locale resolveFromParams( HttpServletRequest request) {
        String lang = request.getParameter(LANG_PARAM);
        if (StrUtil.isNotEmpty(lang)) {
            return parseLocale(lang);
        }

        String locale = request.getParameter(LOCALE_PARAM);
        if (StrUtil.isNotEmpty(locale)) {
            return parseLocale(locale);
        }

        return null;
    }

    protected Locale resolveFromAcceptLanguage( HttpServletRequest request) {
        String acceptLanguage = request.getHeader(ACCEPT_LANGUAGE_HEADER);
        if (StrUtil.isEmpty(acceptLanguage)) {
            return null;
        }

        String[] languages = acceptLanguage.split(",");
        if (languages.length > 0) {
            String primary = languages[0].trim().split(";")[0].trim();
            return parseLocale(primary);
        }

        return null;
    }

    protected Locale resolveFromCustomHeaders( HttpServletRequest request) {
        String xLang = request.getHeader(X_LANG_HEADER);
        if (StrUtil.isNotEmpty(xLang)) {
            return parseLocale(xLang);
        }

        String xLocale = request.getHeader(X_LOCALE_HEADER);
        if (StrUtil.isNotEmpty(xLocale)) {
            return parseLocale(xLocale);
        }

        return null;
    }

    protected Locale parseLocale(String localeStr) {
        if (StrUtil.isEmpty(localeStr)) {
            return null;
        }

        String normalized = localeStr.replace('-', '_');
        String[] parts = normalized.split("_");
        if (parts.length >= 2) {
            return new Locale(parts[0], parts[1]);
        } else if (parts.length == 1 && !parts[0].isEmpty()) {
            return new Locale(parts[0]);
        }

        return null;
    }

    protected HttpServletRequest getCurrentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes)attributes).getRequest();
        }
        return null;
    }
}
