package com.tbs.common.data;

import com.tbs.common.function.SFunction;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 数据存储接口
 * <p>
 * 该接口定义了通用的数据存储功能，提供了数据的增删改查、清空、状态检查等操作。 支持以键值对的形式存储任意类型的对象，并提供了多种便利方法来操作和访问数据。
 *
 * @author abstergo
 * @see Optional 可选值包装类
 * @see Collection 集合接口
 * @see Map.Entry 键值对条目接口
 */
public interface IDataHolder {
	/**
	 * 存储数据
	 * <p>
	 * 将指定的值与键关联并存储。如果force参数为true，则强制存储， 即使该键已存在也会覆盖原有值；如果force为false，则只有在键不存在时才存储。
	 *
	 * @param key   存储数据的键，不能为null
	 * @param value 要存储的数据，可以为null
	 * @param force 是否强制存储，true表示强制覆盖，false表示仅在键不存在时存储
	 * @return 存储成功返回true，否则返回false
	 */
	boolean set(String key, Object value, boolean force);

	/**
	 * 获取数据，如果不存在则计算并存储
	 * <p>
	 * 如果指定键存在，则返回对应的值；如果不存在，则使用提供的函数计算新值， 存储该值并返回。这是一个原子操作，避免了并发环境下的竞态条件问题。
	 *
	 * @param key      数据键，不能为null
	 * @param function 用于计算新值的函数，当键不存在时调用，不能为null
	 * @param <T>      返回值的类型
	 * @return 键对应的现有值或新计算的值
	 * @see SFunction 函数式接口
	 */
	default <T> T computeIfAbsent(String key, SFunction<String, T> function) {
		if (containsKey(key)) {
			return (T)get(key).orElse(null);
		} else {
			T value = function.apply(key);
			set(key, value, true);
			return value;
		}
	}

	/**
	 * 获取数据
	 * <p>
	 * 根据指定的键获取对应的值，以Optional形式返回，避免返回null值。 如果键不存在或值为null，则返回空的Optional。
	 *
	 * @param key 数据键，不能为null
	 * @param <T> 返回值的类型
	 * @return 包含数据值的Optional对象，如果键不存在或值为null则为空
	 * @see Optional 可选值包装类
	 */
	<T> Optional<T> get(String key);

	/**
	 * 删除数据
	 * <p>
	 * 根据指定的键删除对应的键值对数据。
	 *
	 * @param key 要删除数据的键，不能为null
	 */
	void remove(String key);

	/**
	 * 清空数据
	 * <p>
	 * 删除所有存储的数据，使数据持有者变为空状态。
	 */
	void clear();

	/**
	 * 数据是否为空
	 * <p>
	 * 检查数据持有者是否不包含任何数据。
	 *
	 * @return 如果不包含任何数据返回true，否则返回false
	 */
	boolean isEmpty();

	/**
	 * 数据大小
	 * <p>
	 * 返回数据持有者中存储的键值对数量。
	 *
	 * @return 存储的键值对数量
	 */
	int size();

	/**
	 * 数据是否包含某个key
	 * <p>
	 * 检查数据持有者是否包含指定的键。
	 *
	 * @param key 要检查的键，不能为null
	 * @return 如果包含该键返回true，否则返回false
	 */
	boolean containsKey(String key);

	/**
	 * 获取所有数据
	 * <p>
	 * 返回数据持有者中存储的所有键值对条目集合。
	 *
	 * @return 包含所有键值对条目的集合
	 * @see Map.Entry 键值对条目接口
	 * @see Collection 集合接口
	 */
	Collection<Map.Entry<String, Object>> entries();

	/**
	 * 获取所有key
	 * <p>
	 * 返回数据持有者中存储的所有键的集合。
	 *
	 * @return 包含所有键的集合
	 * @see Collection 集合接口
	 */
	Collection<String> keys();

	/**
	 * 获取所有value
	 * <p>
	 * 返回数据持有者中存储的所有值的集合。
	 *
	 * @return 包含所有值的集合
	 * @see Collection 集合接口
	 */
	Collection<Object> values();
}
