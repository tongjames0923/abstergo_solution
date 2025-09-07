package com.tbs.web.security.issuance.center;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Resource;

import org.jetbrains.annotations.NotNull;

import com.tbs.common.IStartup;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.web.security.token.ITokenModel;

/**
 * 抽象数据持有者Token颁发中心
 *
 * @author abstergo
 */
public abstract class AbstractDataHolderTokenIssuanceCenter extends AbstractTokenIssuanceCenter implements IStartup {

	@Resource
	private IDataHolderManager dataHolderManager;

	private IDataHolder manager;

	private IDataHolder tokenSource;

	@Override
	public void startUp() throws RuntimeException {
		manager = dataHolderManager.getDataHolder("token")
				.orElseThrow(() -> new UnsupportedOperationException("未找到数据源以支持令牌管理数据"));
		tokenSource = dataHolderManager.getDataHolder("tokenSource")
				.orElseThrow(() -> new UnsupportedOperationException("未找到数据源以支持令牌管理数据"));
	}

	private String manageKey(String userName, int tokenType) {
		return userName + ":" + tokenType;
	}

	@Override
	protected Collection<String> getUserNameAndTokenType(@NotNull String userName, int tokenType) {
		return manager.<Collection<String>>get(manageKey(userName, tokenType)).orElse(new HashSet<>(4));
	}

	@Override
	protected void saveManageInfo(String userName, int tokenType, String token) {
		Collection<String> userNameAndTokenType = getUserNameAndTokenType(userName, tokenType);
		userNameAndTokenType.add(token);
		manager.set(manageKey(userName, tokenType), userNameAndTokenType, true);
	}

	@Override
	protected void saveToken(ITokenModel model) {
		tokenSource.set(model.getToken(), model, true);
	}

	@Override
	protected ITokenModel getModel(@NotNull String token) {
		return tokenSource.<ITokenModel>get(token).orElse(null);
	}
}
