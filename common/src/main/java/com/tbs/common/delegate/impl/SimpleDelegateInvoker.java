package com.tbs.common.delegate.impl;

import jakarta.annotation.Resource;

import com.tbs.common.delegate.AbstractDelegateInvoker;
import com.tbs.common.delegate.IDelegateFlag;
import com.tbs.common.delegate.IDelegateTarget;
import com.tbs.common.delegate.provider.IDelegateFlagProvider;
import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.function.SSupplier;

/**
 * 简单代理执行器实现类
 * <p>
 * 该类是AbstractDelegateInvoker的实现类，提供了简单的代理执行功能， 直接在调用者线程中同步执行代理任务，不涉及异步或多线程处理。
 * 通过Spring的依赖注入机制获取代理执行标记提供者，并实现具体的读写操作执行逻辑， 适用于简单的同步代理执行场景。
 *
 * @param <T> 目标对象的类型
 * @author tongj
 * @see AbstractDelegateInvoker 抽象代理执行器
 * @see IDelegateFlagProvider 代理执行标记提供者接口
 * @see IDelegateFlag 代理执行标记接口
 * @see IDelegateTarget 代理目标接口
 */
public class SimpleDelegateInvoker<T> extends AbstractDelegateInvoker<T> {

	/**
	 * 代理执行标记提供者
	 * <p>
	 * 通过Spring的@Resource注解自动注入，用于创建和管理代理任务的执行状态跟踪标记。
	 *
	 * @see IDelegateFlagProvider 代理执行标记提供者接口
	 * @see Resource 资源注入注解
	 */
	@Resource
	private IDelegateFlagProvider delegateFlagProvider;

	/**
	 * 获取委托执行标志提供者
	 * <p>
	 * 实现AbstractDelegateInvoker抽象方法，返回通过Spring注入的代理执行标记提供者实例， 用于创建和管理代理任务的执行状态跟踪标记。
	 *
	 * @return 委托执行标志提供者实例
	 * @see AbstractDelegateInvoker#getDelegateFlagProvider() 抽象代理执行器方法
	 * @see IDelegateFlagProvider 代理执行标记提供者接口
	 */
	@Override
	protected IDelegateFlagProvider getDelegateFlagProvider() {
		return delegateFlagProvider;
	}

	/**
	 * 执行委托写入方法
	 * <p>
	 * 实现AbstractDelegateInvoker抽象方法，用于执行具体的代理写入操作。
	 * 直接调用目标对象的write方法进行属性写入，并将执行标记设置为完成状态。
	 *
	 * @param flag   代理执行标记，用于跟踪执行状态
	 * @param method 用于写入目标对象属性的双参数消费者函数
	 * @param p      用于提供写入值的供应者函数
	 * @param <P>    属性值的类型
	 * @see AbstractDelegateInvoker#executeTakeIn(IDelegateFlag, SBiConsumer,
	 *      SSupplier) 抽象代理执行器方法
	 * @see IDelegateFlag 代理执行标记接口
	 * @see IDelegateTarget#write(SBiConsumer, SSupplier) 代理目标写入方法
	 * @see SBiConsumer 双参数消费者函数式接口
	 * @see SSupplier 供应者函数式接口
	 */
	@Override
	protected <P> void executeTakeIn(IDelegateFlag<T> flag, SBiConsumer<T, P> method, SSupplier<P> p) {
		getTarget().write(method, p);
		flag.setFinished(this);
	}

	/**
	 * 执行委托读取方法
	 * <p>
	 * 实现AbstractDelegateInvoker抽象方法，用于执行具体的代理读取操作。
	 * 直接调用目标对象的read方法进行属性读取，并将执行标记设置为完成状态。
	 *
	 * @param flag     代理执行标记，用于跟踪执行状态
	 * @param method   用于读取目标对象属性的函数式方法
	 * @param consumer 用于处理读取到的属性值的消费者函数
	 * @param <P>      属性值的类型
	 * @see AbstractDelegateInvoker#executeTakeOut(IDelegateFlag, SFunction,
	 *      SConsumer) 抽象代理执行器方法
	 * @see IDelegateFlag 代理执行标记接口
	 * @see IDelegateTarget#read(SFunction, SConsumer) 代理目标读取方法
	 * @see SFunction 函数式接口
	 * @see SConsumer 消费者函数式接口
	 */
	@Override
	protected <P> void executeTakeOut(IDelegateFlag<T> flag, SFunction<T, P> method, SConsumer<P> consumer) {
		getTarget().read(method, consumer);
		flag.setFinished(this);
	}
}
