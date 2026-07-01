package com.tbs.common.delegate;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;

/**
 * 代理执行标记接口
 * <p>
 * 该接口定义了代理执行任务的状态跟踪和管理功能，用于监控代理任务的执行过程、结果和异常情况。
 * 提供了获取执行对象、等待执行完成、设置执行状态、异常处理等方法，支持对代理任务执行过程的全面控制和管理。
 *
 * @param <T> 目标对象的类型
 * @author tongj
 * @see IDelegateTarget 代理目标接口
 * @see IDelegateInvoker 代理执行器接口
 * @see AssertUtil 断言工具类
 * @see FormattedRuntimeException 格式化运行时异常类
 */
public interface IDelegateFlag<T> {

	/**
	 * 获取代理执行对象
	 * <p>
	 * 返回与当前执行标记关联的代理目标对象实例，用于获取执行任务的目标对象。
	 *
	 * @return 代理目标对象实例
	 * @see IDelegateTarget 代理目标接口
	 */
	IDelegateTarget<T> get();

	/**
	 * 等待执行完成
	 * <p>
	 * 阻塞当前线程直到关联的代理任务执行完成，返回当前执行标记实例以支持链式调用。
	 *
	 * @return 当前代理执行标记实例，用于链式调用
	 */
	IDelegateFlag<T> waitUntilFinished();

	/**
	 * 设置执行完成状态
	 * <p>
	 * 将当前执行标记的状态设置为完成，并关联触发该状态变更的执行器实例。
	 *
	 * @param invoker 触发状态变更的代理执行器实例，不能为null
	 * @return 如果状态设置成功返回true，否则返回false
	 * @see IDelegateInvoker 代理执行器接口
	 */
	boolean setFinished(IDelegateInvoker<T> invoker);

	/**
	 * 设置执行异常状态
	 * <p>
	 * 将当前执行标记的状态设置为异常，并关联触发该状态变更的执行器实例和异常信息。
	 *
	 * @param e       执行过程中发生的异常，不能为null
	 * @param invoker 触发异常的代理执行器实例，不能为null
	 * @return 如果状态设置成功返回true，否则返回false
	 * @see IDelegateInvoker 代理执行器接口
	 */
	boolean setException(Throwable e, IDelegateInvoker<T> invoker);

	/**
	 * 获取执行异常信息
	 * <p>
	 * 返回执行过程中发生的异常信息，如果执行正常完成则返回null。
	 *
	 * @return 执行过程中发生的异常，如果无异常则返回null
	 */
	Throwable getException();

	/**
	 * 检查执行是否完成
	 * <p>
	 * 检查关联的代理任务是否已经执行完成，无论正常完成还是异常完成都返回true。
	 *
	 * @return 如果执行完成返回true，否则返回false
	 */
	boolean isFinished();

	/**
	 * 异常时抛出格式化运行时异常
	 * <p>
	 * 默认方法实现，检查执行过程中是否发生异常，如果存在异常则抛出格式化的运行时异常， 异常信息包含原始异常和"delegate
	 * error"描述信息。如果无异常则返回当前实例以支持链式调用。
	 *
	 * @return 当前代理执行标记实例，用于链式调用
	 * @throws FormattedRuntimeException 当执行过程中发生异常时抛出
	 * @see AssertUtil#throwError(java.util.function.Supplier) 断言工具类抛出异常方法
	 * @see FormattedRuntimeException 格式化运行时异常类
	 */
	default IDelegateFlag<T> exceptionThenThrow() throws FormattedRuntimeException {
		if (getException() != null) {
			AssertUtil.throwError(() -> new FormattedRuntimeException(getException(), "delegate error"));
		}
		return this;
	}
}
