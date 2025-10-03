package com.tbs.web.config;

import com.tbs.common.util.BeanUtil;
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

import javax.annotation.Resource;


/**
 * token设置
 *
 * @author abstergo
 */
@Configuration
@ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
public class TokenSetConfig {

	@Resource
	WebAuthProperty webAuthProperty;

	@Bean(name = WebBeanNameConstant.LOGIN_TOKEN_PICKER)

    public ITokenPicker tokenPicker() {
		return BeanUtil.buildBeanFromProperties(() -> {
			return new DefaultLoginTokenPicker();
		}, webAuthProperty.getTokenPickerImpl(), BeanUtil::buildByEmptyArgs);
	}

	@Bean
	@ConditionalOnMissingBean(ITokenIssuanceCenter.class)
	ITokenIssuanceCenter tokenIssuanceCenter() {
		return BeanUtil.buildBeanFromProperties(() -> {
			return new NoImplTokenIssuanceCenter();
		}, webAuthProperty.getTokenIssuanceCenterImpl(), BeanUtil::buildByEmptyArgs);
	}

	@Bean
    public TokenHandleFilter tokenPickFilter() {
		return new TokenHandleFilter();
	}
}
