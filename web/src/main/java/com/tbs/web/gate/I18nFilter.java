package com.tbs.web.gate;

import com.tbs.common.i18n.I18nContextHolder;
import com.tbs.common.util.base.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * 国际化语言过滤器
 * <p>
 * 在请求开始时解析客户端语言偏好，设置到I18nContextHolder中，
 * 确保整个请求生命周期（包括Spring Security过滤器链）都能正确获取Locale。
 * <p>
 * 语言解析优先级（从高到低）：
 * 1. 请求参数 ?lang=zh_CN 或 ?locale=en_US
 * 2. 请求头 Accept-Language
 * 3. 请求头 X-Lang 或 X-Locale
 * 4. 默认语言（zh_CN）
 *
 * @author abstergo
 * @see OncePerRequestFilter
 * @see I18nContextHolder
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // 在Spring Security之前执行
public class I18nFilter extends OncePerRequestFilter {

    private static final String LANG_PARAM = "lang";
    private static final String LOCALE_PARAM = "locale";
    private static final String X_LANG_HEADER = "X-Lang";
    private static final String X_LOCALE_HEADER = "X-Locale";
    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Locale locale = resolveLocale(request);
            if (locale != null) {
                I18nContextHolder.setLocale(locale);
                log.debug("I18nFilter设置请求语言: {}", locale);
            }
        } catch (Exception e) {
            log.warn("I18nFilter解析请求语言失败", e);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清理，防止内存泄漏
            I18nContextHolder.clear();
        }
    }

    /**
     * 从请求中解析Locale
     */
    protected Locale resolveLocale(HttpServletRequest request) {
        // 1. 从请求参数获取
        Locale locale = resolveFromParams(request);
        if (locale != null) {
            return locale;
        }

        // 2. 从Accept-Language头获取
        locale = resolveFromAcceptLanguage(request);
        if (locale != null) {
            return locale;
        }

        // 3. 从自定义头获取
        locale = resolveFromCustomHeaders(request);
        if (locale != null) {
            return locale;
        }

        return null;
    }

    protected Locale resolveFromParams(HttpServletRequest request) {
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

    protected Locale resolveFromAcceptLanguage(HttpServletRequest request) {
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

    protected Locale resolveFromCustomHeaders(HttpServletRequest request) {
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
}