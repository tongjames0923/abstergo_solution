package com.tbs.web.security.issuance.center.impl;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import com.tbs.common.util.framework.UuidUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.security.issuance.center.AbstractDataHolderTokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.token.model.SimpleTokenModel;

/**
 * 默认数据持有者令牌颁发中心
 *
 * @author abstergo
 */
public class SimpleDataHolderTokenIssuanceCenter extends AbstractDataHolderTokenIssuanceCenter {

	@Resource
	WebAuthProperty webAuthProperty;

	@Override
	protected ITokenModel createModel(@NotNull UserDetails userDetails, int tokenType, int tokenCreateType) {
		return SimpleTokenModel.create(UuidUtil.getInstance().getUuidString(),
				new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(webAuthProperty.getTokenExpireTime())));
	}

}
