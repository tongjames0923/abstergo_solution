package com.tbs.web.config;

import com.tbs.common.util.BeanUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.config.property.WebProperty;
import com.tbs.web.security.ITokenPicker;
import com.tbs.web.security.impl.DefaultTokenPicker;
import com.tbs.web.security.impl.TokenAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * token设置
 *
 * @author abstergo
 */
@Configuration
public class TokenSetConfig {

    @Resource
    WebAuthProperty webAuthProperty;

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    public ITokenPicker tokenPicker() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new DefaultTokenPicker();
        }, webAuthProperty.getTokenPickerImpl(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }
}
