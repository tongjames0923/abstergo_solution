package com.tbs.common.delegate.impl;

import com.tbs.common.data.IFeatureWrapper;
import com.tbs.common.delegate.IDelegateTarget;
import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.function.SSupplier;

/**
 * 调用者运行代理目标实现类
 * <p>
 * 该类是IDelegateTarget接口的默认实现，采用调用者线程直接执行的方式处理代理目标对象的读写操作。
 * 在调用者线程中直接执行属性读取和写入操作，不涉及额外的线程调度或异步处理，
 * 适用于简单的代理场景或需要同步执行的场景。实现了对目标对象的包装和安全访问控制。
 *
 * @param <T> 目标对象的类型
 * @author tongj
 * @see IDelegateTarget 代理目标接口
 * @see SFunction 函数式接口
 * @see SConsumer 消费者函数式接口
 * @see SBiConsumer 双参数消费者函数式接口
 * @see SSupplier 供应者函数式接口
 */
public class CallerRunsDelegateTarget<T> implements IDelegateTarget<T> {

	/**
	 * 被代理的目标对象
	 * <p>
	 * 实际需要进行代理操作的对象实例，所有读写操作都将直接作用于此对象。
	 */
	private T targetObject;

	/**
	 * 构造函数
	 * <p>
	 * 使用指定的目标对象创建代理目标实例。
	 *
	 * @param targetObject 要被代理的目标对象，可以为null
	 */
	public CallerRunsDelegateTarget(T targetObject) {
		this.targetObject = targetObject;
	}

	/**
	 * 设置原始目标对象
	 * <p>
	 * 实现IFeatureWrapper接口的方法，用于更新被代理的原始目标对象实例。
	 *
	 * @param targetObject 新的目标对象实例，可以为null
	 * @see IFeatureWrapper#setOriginal(Object) 功能包装器设置原始对象方法
	 */
	@Override
	public void setOriginal(T targetObject) {
		this.targetObject = targetObject;
	}

	/**
	 * 读取目标对象的属性值
	 * <p>
	 * 实现IDelegateTarget接口的方法，在调用者线程中直接执行属性读取操作。
	 * 通过指定的函数式方法读取目标对象的特定属性值，并将读取到的值传递给消费者函数进行处理。
	 *
	 * @param method   用于读取目标对象属性的函数式方法，不能为null
	 * @param consumer 用于处理读取到的属性值的消费者函数，不能为null
	 * @param <P>      属性值的类型
	 * @see IDelegateTarget#read(SFunction, SConsumer) 代理目标读取方法
	 * @see SFunction 函数式接口
	 * @see SConsumer 消费者函数式接口
	 */
	@Override
	public <P> void read(SFunction<T, P> method, SConsumer<P> consumer) {
		consumer.accept(method.apply(targetObject));
	}

	/**
	 * 写入目标对象的属性值
	 * <p>
	 * 实现IDelegateTarget接口的方法，在调用者线程中直接执行属性写入操作。
	 * 通过指定的双参数消费者函数设置目标对象的特定属性值，值由供应者函数提供。
	 *
	 * @param setter 用于写入目标对象属性的双参数消费者函数，不能为null
	 * @param val    用于提供写入值的供应者函数，不能为null
	 * @param <P>    属性值的类型
	 * @see IDelegateTarget#write(SBiConsumer, SSupplier) 代理目标写入方法
	 * @see SBiConsumer 双参数消费者函数式接口
	 * @see SSupplier 供应者函数式接口
	 */
	@Override
	public <P> void write(SBiConsumer<T, P> setter, SSupplier<P> val) {
		setter.accept(targetObject, val.get());
	}

	/**
	 * 获取原始目标对象
	 * <p>
	 * 实现IFeatureWrapper接口的方法，返回被代理的原始目标对象实例。
	 *
	 * @return 被代理的原始目标对象实例
	 * @see IFeatureWrapper#getOriginal() 功能包装器获取原始对象方法
	 */
	@Override
	public T getOriginal() {
		return targetObject;
	}
}
