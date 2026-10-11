package com.tbs.web.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.constants.WebBeanNameConstant;
import com.tbs.web.gate.ITokenPicker;
import com.tbs.web.gate.TokenHandleFilter;
import com.tbs.web.security.impl.DefaultLoginTokenPicker;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.issuance.impl.NoImplTokenIssuanceCenter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import jakarta.annotation.Resource;

/**
 * Token 设置
 * <p>
 * 在 {@code tbs.web.auth.enable=true} 时装配令牌处理相关 Bean：令牌拾取器、令牌颁发中心、令牌处理过滤器。
 * 拾取器/颁发中心实现类可由 {@link WebAuthProperty} 动态指定，均允许缺省实现。
 * </p>
 *
 * @author abstergo
 * @see WebAuthProperty 认证配置属性
 */
@Configuration
public class TokenSetConfig {

    @Resource
    WebAuthProperty webAuthProperty;

    @Bean(name = WebBeanNameConstant.LOGIN_TOKEN_PICKER)
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true", matchIfMissing = true)
    public ITokenPicker tokenPicker() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new DefaultLoginTokenPicker();
        }, webAuthProperty.getTokenPickerImpl(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(ITokenIssuanceCenter.class)
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true", matchIfMissing = true)
    ITokenIssuanceCenter tokenIssuanceCenter() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new NoImplTokenIssuanceCenter();
        }, webAuthProperty.getTokenIssuanceCenterImpl(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true", matchIfMissing = true)
    public TokenHandleFilter tokenPickFilter() {
        return new TokenHandleFilter();
    }

    /**
     * 关闭 Spring Boot 对 {@link TokenHandleFilter} 的容器级自动注册。
     * <p>
     * {@code TokenHandleFilter} 作为 {@code Filter} 类型的 Bean 会被 Boot 自动注册为 Servlet 容器过滤器，
     * 在 Spring Security 过滤器链之前执行；此时它写入 {@code SecurityContextHolder} 的认证信息会被链内的
     * {@code SecurityContextHolderFilter} 覆盖清除，而其本应生效的链内位置（{@code addFilterBefore}）又因
     * {@code OncePerRequestFilter} 的去重被跳过，最终导致受保护接口拿到匿名身份而 401。
     * 禁用自动注册后，该过滤器仅在安全链内按 {@code WebConfig} 编排的位置执行一次。
     * </p>
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<TokenHandleFilter> tokenHandleFilterRegistration(TokenHandleFilter filter) {
        FilterRegistrationBean<TokenHandleFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
