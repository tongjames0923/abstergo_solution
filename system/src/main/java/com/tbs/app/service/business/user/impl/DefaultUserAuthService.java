package com.tbs.app.service.business.user.impl;

import java.util.List;

import javax.annotation.Resource;

import com.tbs.app.service.business.user.IPasswordEncrypt;
import com.tbs.app.service.business.user.ISystemUserService;
import com.tbs.common.util.CollUtil;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.AbstractUserAuthService;

/**
 * @author abstergo
 */
public class DefaultUserAuthService extends AbstractUserAuthService {

	@Resource
	private ISystemUserService userService;

	@Resource
	IPasswordEncrypt passwordEncrypt;

	@Override
	protected boolean validate(String username, String password) {
		List<SystemUserEntity> users = userService.queryByUserName(username);
		if (CollUtil.isEmpty(users)) {
			return false;
		}
		SystemUserEntity user = CollUtil.first(users);
		String encryptedPassword = passwordEncrypt.encrypt(password);
		if (user.getPassword().equals(encryptedPassword)) {
			return true;
		}
		return false;
	}

	@Override
	public List<ITokenModel> getTokensByUserName(String username) {
		return List.of();
	}
}
