package com.tbs.web.security.issuance.center;

import javax.annotation.Resource;

import org.jetbrains.annotations.NotNull;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.common.error.AssertUtil;
import com.tbs.web.security.token.ITokenModel;

/**
 * 抽象数据持有者Token颁发中心
 *
 * @author abstergo
 */
public abstract class AbstractDataHolderTokenIssuanceCenter extends AbstractTokenIssuanceCenter {

	@Resource
	private IDataHolderManager dataHolderManager;

	protected String getDataHolderName() {
		return "token-data-source";
	}

	private IDataHolder getHolder() {
		var dataHolder = dataHolderManager.getDataHolder(getDataHolderName());
		AssertUtil.isTrue(!dataHolder.isPresent(), "未找到数据源");
		return dataHolder.get();
	}
	@Override
	protected ITokenModel getModel(@NotNull String token) {
		return (ITokenModel) getHolder().get(token).orElse(null);
	}

	@Override
	protected void saveModel(@NotNull ITokenModel model) {
		getHolder().set(model.getToken(), model, true);
	}

}
