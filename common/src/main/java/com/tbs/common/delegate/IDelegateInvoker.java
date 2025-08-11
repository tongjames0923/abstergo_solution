package com.tbs.common.delegate;

import java.util.Collection;

import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.function.SSupplier;

/**
 * 代理执行器接口
 * <p>
 * 该接口定义了代理模式中执行器的功能规范，负责协调和执行对目标对象的代理操作。 提供了设置和获取目标对象、属性读写操作、执行代理任务以及查询执行状态等方法。
 * 通过链式调用的方式支持连续的代理操作，并可以执行单次或批量的代理任务。
 *
 * @param <T> 目标对象的类型
 * @author tongj
 * @see IDelegateTarget 代理目标接口
 * @see IDelegateFlag 代理执行标记接口
 * @see SBiConsumer 双参数消费者函数式接口
 * @see SSupplier 供应者函数式接口
 * @see SFunction 函数式接口
 * @see SConsumer 消费者函数式接口
 */
public interface IDelegateInvoker<T> {

	/**
	 * 设置代理目标对象
	 * <p>
	 * 为当前执行器设置要操作的目标对象，后续的代理操作都将作用于此目标对象。
	 *
	 * @param delegateTarget 代理目标对象，不能为null
	 * @see IDelegateTarget 代理目标接口
	 */
	void setTarget(IDelegateTarget<T> delegateTarget);

	/**
	 * 获取代理目标对象
	 * <p>
	 * 返回当前执行器关联的代理目标对象实例。
	 *
	 * @return 代理目标对象实例
	 * @see IDelegateTarget 代理目标接口
	 */
	IDelegateTarget<T> getTarget();

	/**
	 * 代理设置目标对象属性值
	 * <p>
	 * 通过指定的双参数消费者函数设置目标对象的特定属性值，值由供应者函数提供。 该方法支持链式调用，返回当前执行器实例以支持连续操作。
	 *
	 * @param method 用于写入目标对象属性的双参数消费者函数，不能为null
	 * @param p      用于提供写入值的供应者函数，不能为null
	 * @param <P>    属性值的类型
	 * @return 当前代理执行器实例，用于链式调用
	 * @see SBiConsumer 双参数消费者函数式接口
	 * @see SSupplier 供应者函数式接口
	 */
	<P> IDelegateInvoker<T> takeIn(SBiConsumer<T, P> method, SSupplier<P> p);

	/**
	 * 代理读取目标特定的属性值
	 * <p>
	 * 通过指定的函数式方法读取目标对象的特定属性值，并将读取到的值传递给消费者函数进行处理。 该方法支持链式调用，返回当前执行器实例以支持连续操作。
	 *
	 * @param method   用于读取目标对象属性的函数式方法，不能为null
	 * @param consumer 用于处理读取到的属性值的消费者函数，不能为null
	 * @param <P>      属性值的类型
	 * @return 当前代理执行器实例，用于链式调用
	 * @see SFunction 函数式接口
	 * @see SConsumer 消费者函数式接口
	 */
	<P> IDelegateInvoker<T> takeOut(SFunction<T, P> method, SConsumer<P> consumer);

	/**
	 * 执行单次代理任务
	 * <p>
	 * 执行一次代理操作并返回执行结果标记，用于跟踪和管理代理任务的执行状态和结果。
	 *
	 * @return 代理执行结果标记
	 * @see IDelegateFlag 代理执行标记接口
	 */
	IDelegateFlag<T> executeOnce();

	/**
	 * 执行所有代理任务
	 * <p>
	 * 执行所有已配置的代理操作并返回执行结果标记集合，用于批量处理和跟踪多个代理任务的执行状态和结果。
	 *
	 * @return 代理执行结果标记集合
	 * @see IDelegateFlag 代理执行标记接口
	 * @see Collection 集合接口
	 */
	Collection<IDelegateFlag<T>> executeAll();

	/**
	 * 获取等待执行完成的任务数量
	 * <p>
	 * 返回当前正在等待执行完成的代理任务数量，用于监控和管理代理任务的执行进度。
	 *
	 * @return 等待执行完成的任务数量
	 */
	int waittingCount();

}
