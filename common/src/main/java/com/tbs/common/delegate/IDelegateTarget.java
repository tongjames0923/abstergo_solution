package com.tbs.common.delegate;

import com.tbs.common.data.IFeatureWrapper;
import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.function.SSupplier;

/**
 * 对象代理目标接口
 * <p>
 * 该接口定义了代理模式中目标对象的访问和操作规范，继承自IFeatureWrapper接口，
 * 既是目标对象的包装器又提供了对目标对象属性的读写操作能力。通过函数式接口实现对目标对象的间接访问，
 * 支持类型安全的属性读取和写入操作，避免直接暴露目标对象的内部结构。
 *
 * @param <T> 目标对象的类型
 * @author tongj
 * @see IFeatureWrapper 功能包装接口
 * @see SFunction 函数式接口（用于属性读取）
 * @see SConsumer 消费者函数式接口（用于处理读取的值）
 * @see SBiConsumer 双参数消费者函数式接口（用于属性写入）
 * @see SSupplier 供应者函数式接口（用于提供写入的值）
 */
public interface IDelegateTarget<T> extends IFeatureWrapper<T> {

	/**
	 * 读取目标对象的属性值
	 * <p>
	 * 通过指定的函数式方法读取目标对象的特定属性值，并将读取到的值传递给消费者函数进行处理。 实现了对目标对象属性的安全读取，避免直接访问目标对象的内部结构。
	 *
	 * @param method   用于读取目标对象属性的函数式方法，不能为null
	 * @param consumer 用于处理读取到的属性值的消费者函数，不能为null
	 * @param <P>      属性值的类型
	 * @see SFunction 函数式接口
	 * @see SConsumer 消费者函数式接口
	 */
	<P> void read(SFunction<T, P> method, SConsumer<P> consumer);

	/**
	 * 写入目标对象的属性值
	 * <p>
	 * 通过指定的双参数消费者函数设置目标对象的特定属性值，值由供应者函数提供。 实现了对目标对象属性的安全写入，避免直接修改目标对象的内部结构。
	 *
	 * @param setter 用于写入目标对象属性的双参数消费者函数，不能为null
	 * @param val    用于提供写入值的供应者函数，不能为null
	 * @param <P>    属性值的类型
	 * @see SBiConsumer 双参数消费者函数式接口
	 * @see SSupplier 供应者函数式接口
	 */
	<P> void write(SBiConsumer<T, P> setter, SSupplier<P> val);

}
