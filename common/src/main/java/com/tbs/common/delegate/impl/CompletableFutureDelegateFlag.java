package com.tbs.common.delegate.impl;

import java.util.concurrent.CompletableFuture;

import com.tbs.common.delegate.IDelegateFlag;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.delegate.IDelegateTarget;

/**
 * 基于CompletableFuture的代理执行标记实现类
 * <p>
 * 该类是IDelegateFlag接口的实现，使用CompletableFuture作为异步执行状态管理的核心机制。
 * 通过CompletableFuture提供的异步编程能力，实现对代理任务执行状态的跟踪、等待和异常处理。
 * 支持阻塞等待执行完成、设置执行完成状态、设置执行异常状态等操作，适用于异步代理执行场景。
 *
 * @param <T> 目标对象的类型
 * @author tongj
 * @see IDelegateFlag 代理执行标记接口
 * @see IDelegateTarget 代理目标接口
 * @see IDelegateInvoker 代理执行器接口
 * @see CompletableFuture 异步计算结果类
 */
public class CompletableFutureDelegateFlag<T> implements IDelegateFlag<T> {

	/**
	 * 异步执行结果Future
	 * <p>
	 * 用于跟踪和管理代理任务的异步执行状态，通过CompletableFuture的完成状态表示任务的执行结果。
	 * 当任务正常完成时，通过complete方法设置结果；当任务异常完成时，通过completeExceptionally方法设置异常。
	 *
	 * @see CompletableFuture 异步计算结果类
	 */
	private CompletableFuture<IDelegateTarget<T>> future;

	/**
	 * 代理目标对象
	 * <p>
	 * 与当前执行标记关联的代理目标对象实例，用于获取执行任务的目标对象。
	 *
	 * @see IDelegateTarget 代理目标接口
	 */
	private IDelegateTarget<T> delegateTarget;

	/**
	 * 代理执行器
	 * <p>
	 * 触发当前执行标记状态变更的代理执行器实例，用于验证状态变更的合法性。
	 *
	 * @see IDelegateInvoker 代理执行器接口
	 */
	private IDelegateInvoker<T> invoker;

	/**
	 * 执行异常信息
	 * <p>
	 * 执行过程中发生的异常信息，如果执行正常完成则为null。
	 */
	private Throwable exception = null;

	/**
	 * 构造函数
	 * <p>
	 * 使用指定的代理目标对象和代理执行器创建代理执行标记实例，并初始化CompletableFuture用于状态跟踪。
	 *
	 * @param delegateTarget 代理目标对象实例，不能为null
	 * @param invoker        代理执行器实例，不能为null
	 * @see IDelegateTarget 代理目标接口
	 * @see IDelegateInvoker 代理执行器接口
	 * @see CompletableFuture 异步计算结果类
	 */
	public CompletableFutureDelegateFlag(IDelegateTarget<T> delegateTarget, IDelegateInvoker<T> invoker) {
		this.delegateTarget = delegateTarget;
		this.invoker = invoker;
		future = new CompletableFuture<>();
	}

	/**
	 * 获取代理执行对象
	 * <p>
	 * 实现IDelegateFlag接口的方法，返回与当前执行标记关联的代理目标对象实例，用于获取执行任务的目标对象。
	 *
	 * @return 代理目标对象实例
	 * @see IDelegateFlag#get() 代理执行标记接口方法
	 * @see IDelegateTarget 代理目标接口
	 */
	@Override
	public IDelegateTarget<T> get() {
		return delegateTarget;
	}

	/**
	 * 等待执行完成
	 * <p>
	 * 实现IDelegateFlag接口的方法，阻塞当前线程直到关联的代理任务执行完成，
	 * 返回当前执行标记实例以支持链式调用。通过CompletableFuture的join方法实现阻塞等待。
	 *
	 * @return 当前代理执行标记实例，用于链式调用
	 * @see IDelegateFlag#waitUntilFinished() 代理执行标记接口方法
	 * @see CompletableFuture#join() 异步计算结果等待方法
	 */
	@Override
	public IDelegateFlag<T> waitUntilFinished() {
		future.join();
		return this;
	}

	/**
	 * 设置执行完成状态
	 * <p>
	 * 实现IDelegateFlag接口的方法，将当前执行标记的状态设置为完成，
	 * 并关联触发该状态变更的执行器实例。只有当传入的执行器与初始化时的执行器相同时才执行状态变更操作。
	 *
	 * @param invoker 触发状态变更的代理执行器实例，不能为null
	 * @return 如果状态设置成功返回true，否则返回false
	 * @see IDelegateFlag#setFinished(IDelegateInvoker) 代理执行标记接口方法
	 * @see IDelegateInvoker 代理执行器接口
	 * @see CompletableFuture#complete(Object) 异步计算结果完成方法
	 */
	@Override
	public boolean setFinished(IDelegateInvoker<T> invoker) {
		if (invoker == this.invoker) {
			future.complete(delegateTarget);
			return true;
		}
		return false;
	}

	/**
	 * 设置执行异常状态
	 * <p>
	 * 实现IDelegateFlag接口的方法，将当前执行标记的状态设置为异常，
	 * 并关联触发该状态变更的执行器实例和异常信息。只有当传入的执行器与初始化时的执行器相同时才执行状态变更操作。
	 *
	 * @param e       执行过程中发生的异常，不能为null
	 * @param invoker 触发异常的代理执行器实例，不能为null
	 * @return 如果状态设置成功返回true，否则返回false
	 * @see IDelegateFlag#setException(Throwable, IDelegateInvoker) 代理执行标记接口方法
	 * @see IDelegateInvoker 代理执行器接口
	 * @see CompletableFuture#completeExceptionally(Throwable) 异步计算结果异常完成方法
	 */
	@Override
	public boolean setException(Throwable e, IDelegateInvoker<T> invoker) {
		if (invoker == this.invoker) {
			future.completeExceptionally(e);
			this.exception = e;
			return true;
		}
		return false;
	}

	/**
	 * 获取执行异常信息
	 * <p>
	 * 实现IDelegateFlag接口的方法，返回执行过程中发生的异常信息，如果执行正常完成则返回null。
	 *
	 * @return 执行过程中发生的异常，如果无异常则返回null
	 * @see IDelegateFlag#getException() 代理执行标记接口方法
	 */
	@Override
	public Throwable getException() {
		return exception;
	}

	/**
	 * 检查执行是否完成
	 * <p>
	 * 实现IDelegateFlag接口的方法，检查关联的代理任务是否已经执行完成，
	 * 无论正常完成还是异常完成都返回true。通过CompletableFuture的isDone方法实现状态检查。
	 *
	 * @return 如果执行完成返回true，否则返回false
	 * @see IDelegateFlag#isFinished() 代理执行标记接口方法
	 * @see CompletableFuture#isDone() 异步计算结果完成状态检查方法
	 */
	@Override
	public boolean isFinished() {
		return future.isDone();
	}
}
