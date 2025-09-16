package com.tbs.web.config;

import com.tbs.web.config.property.WebProperty;
import com.tbs.web.constants.WebBeanNameConstant;
import com.tbs.web.gate.ITokenPicker;
import com.tbs.web.gate.TokenHandleFilter;
import com.tbs.web.gate.center.impl.LoginGateWayCenter;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.Resource;

/**
 * Web 配置类
 *
 * 主要负责以下功能配置： 1. Spring Security 安全配置 2. CORS 跨域配置 3. HTTP 安全特性配置（CSRF、HTTP Basic、表单登录等） 4. 请求结果构建器配置
 *
 * @author tongj
 */
@Configuration
@EnableWebMvc
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class WebConfig {

    /**
     * Web 配置属性 用于获取跨域、CSRF等Web相关配置参数
     */
    @Resource
    private WebProperty webProperty;

    /**
     * 安全配置
     *
     * 当配置项 tbs.web.auth.enable 为 true 时，启用认证模式，配置令牌认证过滤器、异常处理、用户详情服务和认证提供者 当配置项 tbs.web.auth.enable 为 false
     * 或未配置时，使用无认证模式，仅配置异常处理
     *
     * @param http                      Security HTTP 安全对象
     * @param tokenAuthenticationFilter 令牌认证过滤器
     * @param authenticationEntryPoint  认证入口点
     * @param accessDeniedHandler       访问拒绝处理器
     * @param provider                  默认用户认证服务提供者
     * @param userDetailsService        用户详情服务
     * @param authPropertyEnabled       认证是否启用
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityWebFilterChain(HttpSecurity http,
        AuthenticationEntryPoint authenticationEntryPoint, AccessDeniedHandler accessDeniedHandler,
        @Nullable AuthenticationProvider provider, @Value("${tbs.web.auth.enable:false}") boolean authPropertyEnabled,
        TokenHandleFilter filter) throws Exception {
        setSecurity(http);

        if (authPropertyEnabled && filter != null && provider != null) {
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
            http.authenticationProvider(provider);
        }

        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler);

        return http.build();
    }

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
        // 1 设置访问源地址
        corsConfiguration.addAllowedOrigin("*");
        // 2 设置访问源请求头
        corsConfiguration.addAllowedHeader("*");
        // 3 设置访问源请求方法
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setMaxAge(24 * 60 * 60L);
        // corsConfiguration.setAllowCredentials(true);
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

        var letgo = webProperty.getPermitAllPaths().toArray(new String[0]);
        http.authorizeHttpRequests().antMatchers(letgo).permitAll();

        var securedPaths = webProperty.getSecuredPaths().toArray(new String[0]);
        http.authorizeHttpRequests().antMatchers(securedPaths).authenticated();

    }

}
