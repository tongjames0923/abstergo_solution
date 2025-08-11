package com.tbs.common.data;

/**
 * 功能包装接口
 * <p>
 * 该接口定义了一个通用的包装器模式，用于包装和管理特定类型的对象。 通过该接口可以获取和设置被包装的原始对象，实现对对象的间接访问和控制。
 *
 * @param <T> 被包装对象的类型
 * @author abstergo
 */
public interface IFeatureWrapper<T> {
	/**
	 * 获取包装的原始对象
	 * <p>
	 * 返回当前包装器中存储的原始对象实例。
	 *
	 * @return 被包装的原始对象，可能为null
	 */
	T getOrginal();

	/**
	 * 设置包装的原始对象
	 * <p>
	 * 更新当前包装器中存储的原始对象实例。
	 *
	 * @param orginal 要设置的原始对象，可以为null
	 */
	void setOrginal(T orginal);
}
