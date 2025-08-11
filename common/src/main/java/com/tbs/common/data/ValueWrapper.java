package com.tbs.common.data;

import java.util.Optional;

/**
 * 值包装类
 * <p>
 * 该类用于包装一个可能为null的值，提供安全的值访问方式。通过Optional避免直接处理null值，
 * 降低空指针异常的风险。支持创建包装实例、安全获取值以及设置新值等操作。
 *
 * @param <T> 被包装的值的类型
 * @author abstergo
 */
public class ValueWrapper<T> {
	private T value;

	/**
	 * 创建一个值包装类实例
	 * <p>
	 * 工厂方法，用于创建并初始化ValueWrapper实例，将指定的值包装在其中。
	 *
	 * @param value 要包装的值，可以为null
	 * @param <T>   被包装的值的类型
	 * @return 包含指定值的ValueWrapper实例
	 */
	public static <T> ValueWrapper<T> of(T value) {
		ValueWrapper<T> wrapper = new ValueWrapper<>();
		wrapper.set(value);
		return wrapper;
	}

	/**
	 * 获取包装的值
	 * <p>
	 * 以Optional的形式返回包装的值，避免直接返回可能为null的值。 如果包装的值为null，则返回空的Optional。
	 *
	 * @return 包含包装值的Optional对象，如果值为null则返回空Optional
	 */
	public Optional<T> get() {
		return Optional.ofNullable(value);
	}

	/**
	 * 获取包装的值，如果为空则抛出异常
	 * <p>
	 * 直接返回包装的值，如果值为null则抛出NullPointerException异常。
	 *
	 * @return 包装的值，不为null
	 * @throws NullPointerException 当包装的值为null时抛出
	 */
	public T getOrThrow() {
		return get().orElseThrow(() -> new NullPointerException("值包装器异常。空值获取"));
	}

	/**
	 * 设置包装的值
	 * <p>
	 * 更新包装器中存储的值，新值可以为null。
	 *
	 * @param value 要设置的新值，可以为null
	 */
	public void set(T value) {
		this.value = value;
	}

}
