package com.tbs.web.config;

import com.tbs.web.config.property.WebProperty;
import com.tbs.web.constants.WebBeanNameConstant;
import com.tbs.web.gate.ITokenPicker;
import com.tbs.web.gate.TokenHandleFilter;
import com.tbs.web.gate.center.impl.LoginGateWayCenter;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.annotation.Resource;

/**
 * Web 配置类
 * <p>
 * 主要负责以下功能配置：
 * <ol>
 *     <li>Spring Security 安全配置（认证入口、访问拒绝、无状态会话、路径放行/受保护）</li>
 *     <li>CORS 跨域配置</li>
 *     <li>HTTP 安全特性（CSRF、HTTP Basic、表单登录、注销）开关</li>
 *     <li>装配登录网关中心等 Web 组件 Bean（上下文传播器见 {@link ContextPropagatorConfig}）</li>
 * </ol>
 * 各项开关由 {@link WebProperty} 驱动。
 * </p>
 *
 * @author tongj
 * @see WebProperty Web 配置属性
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class WebConfig extends WebSecurityConfigurerAdapter {

    @Resource
    AuthenticationEntryPoint authenticationEntryPoint;
    @Resource
    AccessDeniedHandler accessDeniedHandler;
    @Resource
    @Nullable AuthenticationProvider provider;
    @Resource
    TokenHandleFilter filter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        setSecurity(http);
        if (filter != null && provider != null) {
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
            http.authenticationProvider(provider);
        }

        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler);
    }

    /**
     * Web 配置属性 用于获取跨域、CSRF等Web相关配置参数
     */
    @Resource
    private WebProperty webProperty;

    @Bean
    LoginGateWayCenter loginSecurityManager(@Qualifier(WebBeanNameConstant.LOGIN_TOKEN_PICKER) ITokenPicker picker) {
        return new LoginGateWayCenter(picker);
    }

    /**
     * 创建认证管理器
     *
     * @param configuration 认证配置
     * @return 认证管理器
     * @throws Exception 配置异常
     */
    @Bean
    AuthenticationManager manager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 创建 CORS 配置源
     *
     * 配置允许所有来源、所有头、所有方法的跨域请求 设置预检请求缓存时间为24小时
     *
     * @return CORS 配置源
     */
    public CorsConfigurationSource corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 放行全部来源/头/方法（演示默认配置，生产应按需收紧）
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setMaxAge(24 * 60 * 60L);
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    /**
     * 配置 HTTP 安全设置
     *
     * 根据 webProperty 配置启用/禁用各种安全特性
     *
     * @param http Security HTTP 安全对象
     * @throws Exception 配置异常
     */
    private void setSecurity(HttpSecurity http) throws Exception {
        if (webProperty.isEnableCors()) {
            http.cors().configurationSource(corsFilter());
        }
        if (webProperty.isEnableCsrf()) {
            http.csrf();
        } else {
            http.csrf().disable();
        }

        if (webProperty.isEnableHttpBasic()) {
            http.httpBasic();
        } else {
            http.httpBasic().disable();
        }

        if (webProperty.isEnableFormLogin()) {
            http.formLogin().loginPage("/login").permitAll();
        } else {
            http.formLogin().disable();
        }

        if (webProperty.isEnableLogout()) {
            http.logout().permitAll();
        } else {
            http.logout().disable();
        }

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        var letgo = webProperty.getPermitAllPaths().toArray(new String[0]);
        http.authorizeHttpRequests().antMatchers(letgo).permitAll();

        var securedPaths = webProperty.getSecuredPaths().toArray(new String[0]);
        http.authorizeHttpRequests().antMatchers(securedPaths).authenticated();

    }

}
