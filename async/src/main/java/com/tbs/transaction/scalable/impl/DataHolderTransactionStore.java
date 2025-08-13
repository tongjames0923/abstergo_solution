package com.tbs.transaction.scalable.impl;

import java.util.Optional;

import javax.annotation.Resource;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.transaction.ITransactionInfo;
import com.tbs.transaction.scalable.ITransactionStore;

/**
 * 基于DataHolder的事务存储实现类
 * <p>
 * 该类实现了{@link ITransactionStore}接口，使用DataHolder机制来存储和获取事务信息。
 * 通过IDataHolderManager管理数据存储，提供事务信息的持久化能力。
 * </p>
 *
 * @see ITransactionStore 事务存储接口
 * @see IDataHolder 数据持有者接口
 * @see IDataHolderManager 数据持有者管理器接口
 */
public class DataHolderTransactionStore implements ITransactionStore {

	/**
	 * 用于事务存储的DataHolder名称
	 * <p>
	 * 该常量定义了存储事务信息的DataHolder的唯一标识名称。
	 * </p>
	 */
	public static final String DATA_HOLDER_NAME = "DATAHOLDER_FOR_TRANSACTION:data";

	/**
	 * 数据持有者管理器，用于获取和管理数据持有者实例
	 *
	 * @see IDataHolderManager 数据持有者管理器接口
	 */
	@Resource
	private IDataHolderManager dataHolderManager;

	/**
	 * 获取数据持有者实例
	 * <p>
	 * 该方法从数据持有者管理器中获取指定名称的数据持有者实例。 如果未找到对应的数据源，将抛出运行时异常。
	 * </p>
	 *
	 * @return IDataHolder 数据持有者实例
	 * @throws RuntimeException 当未找到指定名称的数据源时抛出
	 * @see IDataHolderManager#getDataHolder(String) 获取数据持有者方法
	 * @see #DATA_HOLDER_NAME 数据持有者名称常量
	 */
	private IDataHolder getDataHolder() {
		return dataHolderManager.getDataHolder(DATA_HOLDER_NAME).orElseThrow(() -> new RuntimeException("未找到数据源"));
	}

	/**
	 * 存储事务信息
	 * <p>
	 * 该方法将事务信息存储到数据持有者中，使用指定的键作为标识。
	 * </p>
	 *
	 * @param key             事务键，用于唯一标识一个事务
	 * @param transactionInfo 事务信息对象，包含事务的完整信息
	 * @return boolean 存储是否成功，true表示存储成功，false表示存储失败
	 * @see ITransactionStore#store(String, ITransactionInfo) 事务存储接口方法
	 * @see IDataHolder#set(String, Object, boolean) 数据持有者设置数据方法
	 */
	@Override
	public boolean store(String key, ITransactionInfo transactionInfo) {
		return getDataHolder().set(key, transactionInfo, true);
	}

	/**
	 * 获取事务信息
	 * <p>
	 * 该方法根据指定的键从数据持有者中获取事务信息。
	 * </p>
	 *
	 * @param key 事务键，用于唯一标识一个事务
	 * @return Optional<ITransactionInfo> 事务信息包装对象，如果不存在则返回空Optional
	 * @see ITransactionStore#get(String) 事务获取接口方法
	 * @see IDataHolder#get(String) 数据持有者获取数据方法
	 */
	@Override
	public Optional<ITransactionInfo> get(String key) {
		return getDataHolder().get(key);
	}
}
