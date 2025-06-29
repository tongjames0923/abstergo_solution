package com.tbs.web.config;

import com.alibaba.fastjson2.JSON;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.config.property.WebProperty;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IAjaxBuilderProvider;
import com.tbs.web.result.impl.SimpleBuilderProvider;
import com.tbs.web.security.ITokenHandler;
import com.tbs.web.security.ITokenPicker;
import com.tbs.web.security.impl.*;
import com.tbs.web.security.model.TokenModel;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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

    @Bean
    @Lazy
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    public ITokenPicker tokenPicker() {
        if (WebAuthProperty.DEFAULT_TOKEN_PICKER.equals(webAuthProperty.getTokenPickerImpl())) {
            return new DefaultTokenPicker();
        }
        return SpringUtil.getBean(webAuthProperty.getTokenPickerImpl(), ITokenPicker.class);
    }

    @Bean
    @ConditionalOnMissingBean(ITokenHandler.class)
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    public ITokenHandler tokenHandler() {
        return new NoImplTokenHandler();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    public AuthExceptionHandler authExceptionHandler() {
        return new AuthExceptionHandler();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "false", matchIfMissing = true)
    public SecurityFilterChain noAuthsecurityWebFilterChain(HttpSecurity http,
        AuthExceptionHandler authExceptionHandler) throws Exception {
        setSecurity(http);
        http.exceptionHandling(authExceptionHandler);

        return http.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    public SecurityFilterChain securityWebFilterChain(HttpSecurity http,
        TokenAuthenticationFilter tokenAuthenticationFilter, AuthExceptionHandler authExceptionHandler)
        throws Exception {
        setSecurity(http);
        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling(authExceptionHandler);
        return http.build();
    }

    private void setSecurity(HttpSecurity http) throws Exception {
        if (webProperty.isEnableCors()) {
            http.cors();
        } else {
            http.cors().disable();
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
        if (webProperty.getResultBuilderProvier().equals(WebProperty.DEFAULT_RESULT_BUILDER_PROVIDER)) {
            return new SimpleBuilderProvider();
        }
        return SpringUtil.getBean(webProperty.getResultBuilderProvier(), IAjaxBuilderProvider.class);
    }

    @Resource
    private WebAuthProperty webAuthProperty;

}
