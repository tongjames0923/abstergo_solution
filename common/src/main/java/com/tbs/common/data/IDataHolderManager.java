package com.tbs.common.data;

import java.util.Collection;
import java.util.Optional;

/**
 * 数据持有者管理器接口
 * <p>
 * 该接口定义了对多个数据持有者(IDataHolder)进行统一管理的功能， 包括数据持有者的注册、注销、获取以及名称管理等操作。
 * 通过该管理器可以集中管理多个数据持有者实例，实现统一的访问和控制。
 *
 * @author abstergo
 * @see IDataHolder 数据持有者接口
 * @see Optional 可选值包装类
 * @see Collection 集合接口
 */
public interface IDataHolderManager {
	/**
	 * 根据名称获取数据持有者
	 * <p>
	 * 根据指定的名称查找对应的数据持有者实例，如果找到则返回包含该实例的Optional， 否则返回空的Optional。
	 *
	 * @param name 数据持有者的名称，不能为null
	 * @return 包含数据持有者的Optional实例，如果未找到则为空
	 * @see IDataHolder 数据持有者接口
	 * @see Optional 可选值包装类
	 */
	Optional<IDataHolder> getDataHolder(String name);

	/**
	 * 获取所有数据持有者的名称
	 * <p>
	 * 返回当前管理器中注册的所有数据持有者的名称集合。
	 *
	 * @return 包含所有数据持有者名称的集合
	 * @see Collection 集合接口
	 */
	Collection<String> getDataHolderNames();

	/**
	 * 注册数据持有者
	 * <p>
	 * 将指定的数据持有者实例以给定名称注册到管理器中， 后续可以通过该名称获取此数据持有者实例。
	 *
	 * @param name       数据持有者的名称，不能为null
	 * @param dataHolder 要注册的数据持有者实例，不能为null
	 * @see IDataHolder 数据持有者接口
	 */
	void registerDataHolder(String name, IDataHolder dataHolder);

	/**
	 * 注销数据持有者
	 * <p>
	 * 根据指定的名称从管理器中移除对应的数据持有者实例。
	 *
	 * @param name 要注销的数据持有者的名称，不能为null
	 * @see IDataHolder 数据持有者接口
	 */
	void unregisterDataHolder(String name);

}
