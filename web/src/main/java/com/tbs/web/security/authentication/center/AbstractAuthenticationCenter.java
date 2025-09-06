package com.tbs.web.security.authentication.center;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.AuthenticationException;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.token.ITokenModel;

/**
 * 抽象认证中心
 *
 * @author abstergo
 */
public abstract class AbstractAuthenticationCenter implements IAuthenticationCenter {

	/**
	 * 创建认证
	 *
	 * @param model
	 *            令牌模型
	 * @return 认证结果
	 * @throws AuthenticationException
	 *             认证异常
	 */
	protected abstract ITokenAuthentication createAuth(@NotNull ITokenModel model);

	@Override
	public ITokenAuthentication createAuthenticationByTokenModel(ITokenModel model)
			throws AuthenticationException, InvalidTokenException {
		AssertUtil.isTrue(model == null, () -> new InvalidTokenException("空令牌实体验证"));
		AssertUtil.isTrue(StrUtil.isEmpty(model.getToken()), () -> new InvalidTokenException("空令牌内容验证"));
		AssertUtil.isTrue(model.getStatus() == null || !(Objects.equals(model.getStatus(), TokenStatusEnum.ACTIVE)),
				() -> new InvalidTokenException("令牌状态验证失败"));
		AssertUtil.isTrue(model.getExpireTime() != null && model.getExpireTime().getTime() < System.currentTimeMillis(),
				() -> {
					return new InvalidTokenException("令牌已过期");
				});
		return createAuth(model);
	}

}
