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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.annotation.Resource;

/**
 * token设置
 *
 * @author abstergo
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
}
