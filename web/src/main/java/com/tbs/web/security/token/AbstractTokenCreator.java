package com.tbs.web.security.token;

import java.time.Duration;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UserDetails;

import com.tbs.common.util.framework.UuidUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.security.token.model.SimpleTokenModel;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractTokenCreator implements ITokenCreator {

	@Getter
	@Setter
	@Resource
	WebAuthProperty authProperty;

	/**
	 * 创建token
	 *
	 * @param userDetails
	 * @return
	 */
	protected SimpleTokenModel createToken(UserDetails userDetails, WebAuthProperty authProperty) {
		var exp = new Date(
				System.currentTimeMillis() + Duration.ofSeconds(authProperty.getTokenExpireTime()).toMillis());
		return SimpleTokenModel.create(UuidUtil.getInstance().getUuidString(), authProperty.getTokenSource(),
				authProperty.getTokenFiledName(), userDetails.getUsername(), exp);
	}

	/**
	 * 拦截token
	 *
	 * @param token
	 * @return
	 */
	protected abstract ITokenModel interceptToken(SimpleTokenModel token);

	@Override
	public ITokenModel createToken(UserDetails userDetails) {
		return interceptToken(createToken(userDetails, authProperty));
	}
}
