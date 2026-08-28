package com.tbs.web.security.authentication.center;

import org.springframework.security.core.AuthenticationException;

import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.security.token.ITokenModel;

/**
 * 认证中心
 * <p>
 * 负责将令牌模型转换为 Spring Security 认证对象，并完成令牌有效性校验与认证处理。
 * 是「令牌 → 认证上下文」转换的核心入口。
 * </p>
 *
 * @author abstergo
 * @see ITokenAuthentication 令牌认证对象
 */
public interface IAuthenticationCenter {

	/**
	 * 由令牌模型创建认证对象
	 * <p>
	 * 解析令牌模型并构造对应的 {@link ITokenAuthentication}；令牌非法或状态异常时抛错。
	 * </p>
	 *
	 * @param model 令牌模型，不能为空
	 * @return 构造完成的认证对象（尚未通过 {@link #authenticate} 校验）
	 * @throws InvalidTokenException 当令牌无效、过期或状态不符时抛出
	 */
	ITokenAuthentication createAuthenticationByTokenModel(ITokenModel model) throws InvalidTokenException;

	/**
	 * 执行认证
	 * <p>
	 * 对认证对象做有效性校验（如令牌签名、过期时间、状态），返回通过认证的上下文。
	 * </p>
	 *
	 * @param authentication 待认证的令牌认证对象，不能为空
	 * @return 通过认证后的认证上下文
	 * @throws AuthenticationException 当认证校验失败时抛出
	 */
	ITokenAuthentication authenticate(ITokenAuthentication authentication) throws AuthenticationException;

}
