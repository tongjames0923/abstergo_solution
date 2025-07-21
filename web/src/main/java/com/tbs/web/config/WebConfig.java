package com.tbs.web.config;

import com.tbs.common.util.BeanUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.config.property.WebProperty;
import com.tbs.web.result.IAjaxBuilderProvider;
import com.tbs.web.result.IOnErrorAjaxProvider;
import com.tbs.web.result.impl.SimpleBuilderProvider;
import com.tbs.web.result.impl.SimpleErrorAjaxProvider;
import com.tbs.web.security.IUserAuthService;
import com.tbs.web.security.impl.AuthExceptionHandler;
import com.tbs.web.security.impl.DefaultUserAuthServiceImpl;
import com.tbs.web.security.impl.NotImplUserAuthService;
import com.tbs.web.security.impl.TokenAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.Resource;

/**
 * Web 配置
 *
 * @author tongj
 */
@Configuration
@EnableWebMvc
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebConfig {

    @Resource
    private WebProperty webProperty;

    @Resource
    private WebAuthProperty webAuthProperty;

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "false", matchIfMissing = true)
    public SecurityFilterChain noAuthsecurityWebFilterChain(HttpSecurity http,
        AuthExceptionHandler authExceptionHandler) throws Exception {
        setSecurity(http);
        http.exceptionHandling().authenticationEntryPoint(authExceptionHandler)
            .accessDeniedHandler(authExceptionHandler);
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    public SecurityFilterChain securityWebFilterChain(HttpSecurity http,
        TokenAuthenticationFilter tokenAuthenticationFilter, AuthExceptionHandler authExceptionHandler,
        DefaultUserAuthServiceImpl provider) throws Exception {
        setSecurity(http);
        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling().authenticationEntryPoint(authExceptionHandler)
            .accessDeniedHandler(authExceptionHandler);
        http.userDetailsService(provider);
        http.authenticationProvider(provider);
        return http.build();
    }

    @Bean
    AuthenticationManager manager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

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
        //        corsConfiguration.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

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

    @Bean
    public IAjaxBuilderProvider ajaxBuilderProvider() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new SimpleBuilderProvider();
        }, webProperty.getResultBuilderProvier(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    public IOnErrorAjaxProvider errorResultBuilderProvider() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new SimpleErrorAjaxProvider();
        }, webProperty.getErrorResultBuilderProvier(), BeanUtil::buildByEmptyArgs);
    }
}
