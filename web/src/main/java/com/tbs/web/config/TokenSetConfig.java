package com.tbs.web.config;

import com.tbs.common.util.BeanUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.config.property.WebProperty;
import com.tbs.web.security.ITokenPicker;
import com.tbs.web.security.impl.DefaultTokenPicker;
import com.tbs.web.security.impl.TokenAuthenticationFilter;
import com.tbs.web.security.impl.TokenPickFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.annotation.Resource;
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
    public TokenAuthenticationFilter tokenAuthenticationFilter(WebProperty property) {
        List<RequestMatcher> matchers = new ArrayList<>(property.getSecuredPaths().size());
        property.getSecuredPaths().stream().forEach(path -> matchers.add(new AntPathRequestMatcher(path)));
        RequestMatcher matcher = new OrRequestMatcher(matchers);
        return new TokenAuthenticationFilter(matcher);
    }

    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    public TokenPickFilter tokenPickFilter() {
        RequestMatcher matcher = null;
        if (CollUtil.isNotEmpty(webAuthProperty.getTokenIgnoreUrls())) {
            matcher = new OrRequestMatcher(webAuthProperty.getTokenIgnoreUrls().stream().map(AntPathRequestMatcher::new)
                .toArray(AntPathRequestMatcher[]::new));

        }
        return new TokenPickFilter(matcher);
    }
}
