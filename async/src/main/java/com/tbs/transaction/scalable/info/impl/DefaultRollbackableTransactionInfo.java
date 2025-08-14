package com.tbs.transaction.scalable.info.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.tbs.common.error.AssertUtil;
import com.tbs.transaction.scalable.info.AbstractRollbackableTransactionInfo;
import com.tbs.transaction.scalable.info.DefaultRollbackableData;
import com.tbs.transaction.scalable.info.IRollbackableData;

/**
 * 默认可回滚事务信息
 * <p>
 * 默认可回滚事务信息类，继承自AbstractRollbackableTransactionInfo抽象类，实现了IRollbackableTransactionInfo接口。
 * </p>
 *
 *
 **/
public class DefaultRollbackableTransactionInfo extends AbstractRollbackableTransactionInfo {

	private Map<String, IRollbackableData> dataMap = new HashMap<>(8);

	/**
	 * 构造函数
	 * <p>
	 * 该构造函数接收事务ID作为参数，并初始化事务ID和事务状态。
	 * </p>
	 *
	 * @param transactionId 事务ID
	 */
	public DefaultRollbackableTransactionInfo(String transactionId) {
		super(transactionId);
	}

	@Override
	protected Optional<IRollbackableData> pollData(String key) {
		AssertUtil.hasText(key, "key不能为空");
		return Optional.ofNullable(dataMap.getOrDefault(key, null));
	}

	@Override
	protected IRollbackableData createRollbackableData(String key) {
		AssertUtil.hasText(key, "key不能为空");
		AssertUtil.isTrue(!dataMap.containsKey(key), "key已存在");
		return dataMap.put(key, new DefaultRollbackableData());
	}
}
