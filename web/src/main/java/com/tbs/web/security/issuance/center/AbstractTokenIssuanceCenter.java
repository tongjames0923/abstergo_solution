package com.tbs.web.security.issuance.center;

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
	protected abstract void saveModel(@NotNull ITokenModel model);

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
		if (model == null)
			return model;
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

}
