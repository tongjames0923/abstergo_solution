package com.tbs.app.service.business.user.impl;

import com.tbs.web.security.user.IPasswordEncrypt;
import com.tbs.app.service.business.user.ISystemUserService;
import com.tbs.common.util.CollUtil;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.ITokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.AbstractUserAuthService;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author abstergo
 */
public class DefaultUserAuthService extends AbstractUserAuthService {

	@Resource
	private ISystemUserService userService;

	@Resource
	IPasswordEncrypt passwordEncrypt;

	@Resource
    ITokenIssuanceCenter tokenIssuanceCenter;

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
    public List<ITokenModel> getTokensByUserNameAndTokenType(String username, TokenTypeEnum type) {
        return tokenIssuanceCenter.getTokensByUserNameAndTokenType(username, type.getCode()).stream()
            .collect(Collectors.toList());
	}
}
