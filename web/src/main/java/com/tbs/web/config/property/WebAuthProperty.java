package com.tbs.web.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.tbs.web.enums.TokenSource;
import com.tbs.web.gate.ITokenPicker;
import com.tbs.web.security.authentication.center.IAuthenticationCenter;
import com.tbs.web.security.impl.DefaultLoginTokenPicker;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;

import lombok.Data;

/**
 * @author abstergo
 */
@Data
@Component
@ConfigurationProperties(prefix = "tbs.web.auth")
public class WebAuthProperty {

	/**
	 * 是否启用
	 */
	private boolean enable = true;

	/**
	 * token来源
	 */
	private TokenSource tokenSource = TokenSource.HEADER;

	/**
	 * token字段名称
	 */
	private String tokenFiledName = "Authorization";

	/**
	 * token有效期,单位：秒
	 */
	private int tokenExpireTime = 3600;

	/**
	 * token获取器实现类
	 */
	private Class<? extends ITokenPicker> tokenPickerImpl = DefaultLoginTokenPicker.class;

	/**
	 * 认证中心实现类
	 */
	private Class<? extends IAuthenticationCenter> authenticationCenterImpl;

	/**
	 * token颁发中心实现类
	 */
	private Class<? extends ITokenIssuanceCenter> tokenIssuanceCenterImpl;

	/**
	 * token数据源名称
	 */
	private String tokenDataSourceName = "TOKEN";

}
