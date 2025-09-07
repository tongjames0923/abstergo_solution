package com.tbs.web.security.issuance.center;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.security.token.ITokenModel;

/**
 * 抽象的令牌颁发中心
 *
 * @author abstergo
 */
public abstract class AbstractTokenIssuanceCenter implements ITokenIssuanceCenter {

	/**
	 * 获取令牌
	 *
	 * @param token
	 * @return
	 */
	protected abstract ITokenModel getModel(@NotNull String token);

	/**
	 * 保存令牌
	 *
	 * @param model
	 */
	protected void saveModel(@NotNull ITokenModel model) {
		AssertUtil.hasText(model.getToken(), "令牌不能为空");
		AssertUtil.hasText(model.getUserName(), "用户名不能为空");
		AssertUtil.isTrue(model.getTokenType() == null, "令牌类型不能为空");
		center.saveManageInfo(model.getUserName(), model.getTokenType().getCode(), model.getToken());
		center.saveToken(model);
	}

	/**
	 * 保存令牌
	 *
	 * @param token
	 * @param model
	 */
	protected abstract void saveToken(ITokenModel model);

	/**
	 * 保存令牌管理信息
	 *
	 * @param userName
	 * @param token
	 * @param tokenType
	 */
	protected abstract void saveManageInfo(String userName, int tokenType, String token);

	/**
	 * 获取用户名及令牌类型
	 *
	 * @param userName
	 * @param tokenType
	 * @return 令牌管理信息
	 */
	protected abstract Collection<String> getUserNameAndTokenType(@NotNull String userName, int tokenType);

	/**
	 * 创建令牌
	 *
	 * @param userDetails
	 * @param tokenType
	 * @param tokenCreateType
	 * @return
	 */
	protected abstract ITokenModel createModel(@NotNull UserDetails userDetails, int tokenType, int tokenCreateType);

	private AbstractTokenIssuanceCenter center;

	public AbstractTokenIssuanceCenter() {
		this.center = this;
	}

	@Override
	public ITokenModel issueToken(@NotNull UserDetails userDetails, int tokenType, int tokenCreateType) {
		ITokenModel model = createModel(userDetails, tokenType, tokenCreateType);
		center.saveModel(model);
		return model;
	}

	@Override
	public ITokenModel getTokenByToken(@NotNull String token) {
		ITokenModel model = center.getModel(token);
		if (model == null) {
			return model;
		}
		if (model.getExpireTime() != null && model.getExpireTime().getTime() < System.currentTimeMillis()) {
			if (TokenStatusEnum.ACTIVE.getCode().equals(model.getStatus().getCode())) {
				center.setTokenStatus(token, TokenStatusEnum.EXPIRED);
			}
		}
		return model;
	}

	@Override
	public void setTokenStatus(@NotNull String token, @NotNull TokenStatusEnum status) {
		ITokenModel oldModel = center.getModel(token);
		AssertUtil.notNull(oldModel, "令牌不存在");
		oldModel.setStatus(status);
		center.saveModel(oldModel);
	}

	@Override
	public Collection<ITokenModel> getTokensByUserNameAndTokenType(String userName, int tokenType) {
		Set<ITokenModel> tokens = new HashSet<>();
		for (String token : center.getUserNameAndTokenType(userName, tokenType)) {
			ITokenModel model = center.getModel(token);
			if (model != null) {
				tokens.add(model);
			}
		}
		return tokens;
	}
}
