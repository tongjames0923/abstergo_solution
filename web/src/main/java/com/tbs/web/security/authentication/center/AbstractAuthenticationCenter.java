package com.tbs.web.security.authentication.center;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.AuthenticationException;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.token.ITokenModel;

/**
 * 抽象认证中心
 * <p>
 * 模板实现 {@link IAuthenticationCenter}：在构造认证对象前统一完成令牌的基础校验
 * （非空、内容非空、状态为生效、未过期），通过后交由子类 {@link #createAuth} 组装认证对象。
 * </p>
 *
 * @author abstergo
 * @see IAuthenticationCenter 认证中心契约
 */
public abstract class AbstractAuthenticationCenter implements IAuthenticationCenter {

	/**
	 * 由令牌模型构造认证对象
	 * <p>
	 * 基础校验已在 {@link #createAuthenticationByTokenModel} 完成，子类只需组装认证上下文。
	 * </p>
	 *
	 * @param model 已通过基础校验的令牌模型，不能为空
	 * @return 构造完成的认证对象
	 * @throws AuthenticationException 当组装认证过程失败时抛出
	 */
	protected abstract ITokenAuthentication createAuth( ITokenModel model);

	@Override
	public ITokenAuthentication createAuthenticationByTokenModel(ITokenModel model)
			throws AuthenticationException, InvalidTokenException {
		// 基础校验链：实体非空 → 内容非空 → 状态生效 → 未过期
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
