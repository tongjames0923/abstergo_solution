package com.tbs.web.config;

import javax.annotation.Resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tbs.common.util.BeanUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.constants.WebBeanNameConstant;
import com.tbs.web.security.impl.DefaultLoginTokenPicker;
import com.tbs.web.security.impl.SimpleTokenCreator;
import com.tbs.web.security.impl.TokenHandleFilter;
import com.tbs.web.security.token.ITokenCreator;
import com.tbs.web.security.token.ITokenPicker;

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
	@ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
	public ITokenPicker tokenPicker() {
		return BeanUtil.buildBeanFromProperties(() -> {
			return new DefaultLoginTokenPicker();
		}, webAuthProperty.getTokenPickerImpl(), BeanUtil::buildByEmptyArgs);
	}

	@Bean
	@ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
	public ITokenCreator tokenCreator() {
		return BeanUtil.buildBeanFromProperties(() -> {
			return new SimpleTokenCreator();
		}, webAuthProperty.getTokenCreatorImpl(), BeanUtil::buildByEmptyArgs);
	}

	@Bean
	@ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
	public TokenHandleFilter tokenPickFilter() {
		return new TokenHandleFilter();
	}
}
