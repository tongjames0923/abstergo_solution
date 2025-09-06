package com.tbs.app.service.security;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;

import com.tbs.cache.annotation.ExpireOn;
import com.tbs.common.IStartup;
import com.tbs.shared.enums.AccessTokenFlagEnum;
import com.tbs.shared.mapper.AccessTokenMapper;
import com.tbs.shared.pojo.entity.SystemAccessTokenEntity;
import com.tbs.shared.util.NetUtil;
import com.tbs.sql.ef.IEntityQueryWrapper;
import com.tbs.sql.utils.QueryUtil;
import com.tbs.web.security.token.ITokenManager;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserInfoService;

public class DbAdvancedTokenManager implements ITokenManager, IStartup {

	private IEntityQueryWrapper<SystemAccessTokenEntity> queryWrapper;

	@Resource
	private AccessTokenMapper accessTokenMapper;

	@Resource
	IUserInfoService userInfoService;

	@Resource
	HttpServletRequest request;

	@Override
	public void submitToken(@NotNull ITokenModel token, @NotNull UserDetails userDetails) {
		SystemAccessTokenEntity entity = new SystemAccessTokenEntity();
		entity.setToken(token.getToken());
		entity.setUserName(userDetails.getUsername());
		entity.setFlag(AccessTokenFlagEnum.ACTIVE.getCode());
		entity.setCreateTime(token.getCreateTime());
		entity.setExpireTime(token.getExpireTime());
		entity.setIpAddress(NetUtil.getIp(request));
		entity.setMacAddress(NetUtil.getMac(request));
		entity.setCreateType(token.getCreateType().getCode());
		accessTokenMapper.insert(entity);
	}

	@Override
	public void removeToken(@NotNull ITokenModel token) {

	}

	@Cacheable(cacheNames = "token", key = "#token.token", unless = "#result == null")
	@ExpireOn(value = 5, timeUnit = TimeUnit.MINUTES)
	@Override
	public UserDetails loadUserInfoByToken(@NotNull ITokenModel token) {

		return null;
	}

	@Override
	public void startUp() throws RuntimeException {
		queryWrapper = QueryUtil.getEntityQueryWrapper(new SystemAccessTokenEntity());
	}
}
