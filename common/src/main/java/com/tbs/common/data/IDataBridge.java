package com.tbs.common.data;

import org.jetbrains.annotations.NotNull;

/**
 * 数据桥接接口
 * <p>
 * 该接口定义了在两种数据类型之间进行转换的标准方法，主要用于处理不可序列化对象的序列化操作。
 * 通过桥接模式，可以将不支持序列化的对象转换为可序列化的形式，以及将序列化数据还原为原始对象。
 * 泛型参数T表示目标类型（通常是可序列化的类型），E表示源类型（可能是不可序列化的类型）。
 *
 * @param <T> 目标数据类型，通常是可序列化的类型
 * @param <E> 源数据类型，可能是不可序列化的类型
 * @author tongj
 */
public interface IDataBridge<T, E> {
	/**
	 * 桥接方法（序列化）
	 * <p>
	 * 将源类型的对象转换为目标类型的对象，通常用于将不可序列化的对象转换为可序列化的形式。
	 *
	 * @param obj 源对象，不能为null
	 * @return 转换后的目标对象
	 * @throws IllegalArgumentException 当obj为null时抛出
	 * @see NotNull 非空注解
	 */
	T bridge(@NotNull E obj);

	/**
	 * 反桥接方法（反序列化）
	 * <p>
	 * 将目标类型的对象转换回源类型的对象，通常用于将序列化数据还原为原始对象。
	 *
	 * @param obj 目标对象，不能为null
	 * @return 转换后的源对象
	 * @throws IllegalArgumentException 当obj为null时抛出
	 * @see NotNull 非空注解
	 */
	E unbridge(@NotNull T obj);
}
