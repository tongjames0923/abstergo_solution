package com.tbs.common.delegate;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.delegate.provider.IDelegateFlagProvider;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.function.SSupplier;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 抽象代理执行器实现类
 * <p>
 * 该类是IDelegateInvoker接口的抽象实现，提供了代理执行器的基本功能框架。
 * 管理代理任务队列，支持按顺序执行代理读写操作，并提供执行状态跟踪机制。 子类需要实现具体的执行方法和委托标记提供者获取方法，以完成特定的代理执行逻辑。
 *
 * @param <T> 目标对象的类型
 * @author tongj
 * @see IDelegateInvoker 代理执行器接口
 * @see IDelegateTarget 代理目标接口
 * @see IDelegateFlag 代理执行标记接口
 * @see IDelegateFlagProvider 代理执行标记提供者接口
 */
public abstract class AbstractDelegateInvoker<T> implements IDelegateInvoker<T> {

	/**
	 * 代理目标对象
	 * <p>
	 * 当前执行器操作的目标对象实例，通过setTarget方法进行设置， 所有的代理读写操作都将作用于此目标对象。
	 *
	 * @see IDelegateTarget 代理目标接口
	 */
	private IDelegateTarget<T> delegateTarget;

	/**
	 * 设置代理目标对象
	 * <p>
	 * 实现IDelegateInvoker接口的方法，为当前执行器设置要操作的目标对象实例， 后续的代理操作都将作用于此目标对象。如果传入null则会抛出异常。
	 *
	 * @param delegateTarget 代理目标对象，不能为null
	 * @throws IllegalArgumentException 当delegateTarget为null时抛出
	 * @see IDelegateInvoker#setTarget(IDelegateTarget) 代理执行器接口方法
	 * @see AssertUtil 断言工具类
	 */
	@Override
	public void setTarget(IDelegateTarget<T> delegateTarget) {
		AssertUtil.notNull(delegateTarget, I18nUtil.get(CommonI18nConstant.DELEGATE_TARGET_REQUIRED));
		this.delegateTarget = delegateTarget;
	}

	/**
	 * 代理任务封装类
	 * <p>
	 * 用于封装代理执行任务的内部数据结构，包含任务的输入输出参数、执行顺序索引和任务类型标识。
	 * 通过泛型支持不同类型的任务参数，并使用Lombok注解简化代码编写。
	 *
	 * @param <A> 输入参数类型
	 * @param <B> 输出参数类型
	 * @see Data Lombok数据注解
	 * @see AllArgsConstructor Lombok全参构造注解
	 */
	@Data
	@AllArgsConstructor
	private static final class InvokeTask<A, B> {
		/**
		 * 输入参数
		 * <p>
		 * 对于写入操作为SBiConsumer类型，对于读取操作为SFunction类型
		 */
		private A in;

		/**
		 * 输出参数
		 * <p>
		 * 对于写入操作为SSupplier类型，对于读取操作为SConsumer类型
		 */
		private B out;

		/**
		 * 任务执行顺序索引
		 * <p>
		 * 用于确定任务在队列中的执行顺序，值越小优先级越高
		 */
		private long index;

		/**
		 * 任务类型标识
		 * <p>
		 * true表示写入操作(takeIn)，false表示读取操作(takeOut)
		 */
		private boolean isIn;
	}

	/**
	 * 任务索引计数器
	 * <p>
	 * 用于生成任务的顺序索引，确保任务按添加顺序执行，使用原子长整型保证线程安全。
	 *
	 * @see AtomicLong 原子长整型类
	 */
	private AtomicLong index = new AtomicLong(0);

	/**
	 * 获取委托执行标志提供者
	 * <p>
	 * 抽象方法，由子类实现，用于获取代理执行标记的提供者实例， 用于创建和管理代理任务的执行状态跟踪标记。
	 *
	 * @return 委托执行标志提供者实例
	 * @see IDelegateFlagProvider 代理执行标记提供者接口
	 */
	protected abstract IDelegateFlagProvider getDelegateFlagProvider();

	/**
	 * 代理任务队列
	 * <p>
	 * 使用线程安全的并发队列存储待执行的代理任务，按照任务添加顺序执行。 使用AtomicLong索引保证任务按添加顺序入队，ConcurrentLinkedQueue保证并发安全。
	 *
	 * @see ConcurrentLinkedQueue 并发链接队列
	 * @see InvokeTask 代理任务封装类
	 */
	private Queue<InvokeTask> tasks = new ConcurrentLinkedQueue<>();

	/**
	 * 获取代理目标对象
	 * <p>
	 * 实现IDelegateInvoker接口的方法，返回当前执行器关联的代理目标对象实例。
	 *
	 * @return 代理目标对象实例
	 * @see IDelegateInvoker#getTarget() 代理执行器接口方法
	 * @see IDelegateTarget 代理目标接口
	 */
	@Override
	public IDelegateTarget<T> getTarget() {
		return delegateTarget;
	}

	/**
	 * 代理设置目标对象属性值
	 * <p>
	 * 实现IDelegateInvoker接口的方法，通过指定的双参数消费者函数设置目标对象的特定属性值，
	 * 值由供应者函数提供。将任务封装后添加到任务队列中，并支持链式调用返回当前执行器实例。
	 *
	 * @param method 用于写入目标对象属性的双参数消费者函数，不能为null
	 * @param p      用于提供写入值的供应者函数，不能为null
	 * @param <P>    属性值的类型
	 * @return 当前代理执行器实例，用于链式调用
	 * @throws IllegalArgumentException 当method或p为null时抛出
	 * @see IDelegateInvoker#takeIn(SBiConsumer, SSupplier) 代理执行器接口方法
	 * @see SBiConsumer 双参数消费者函数式接口
	 * @see SSupplier 供应者函数式接口
	 * @see AssertUtil 断言工具类
	 */
	@Override
	public <P> IDelegateInvoker<T> takeIn(SBiConsumer<T, P> method, SSupplier<P> p) {
		AssertUtil.notNull(method, I18nUtil.get(CommonI18nConstant.DELEGATE_METHOD_REQUIRED));
		AssertUtil.notNull(p, I18nUtil.get(CommonI18nConstant.DELEGATE_PROVIDER_REQUIRED));
		tasks.add(new InvokeTask<>(method, p, index.addAndGet(1), true));
		return this;
	}

	/**
	 * 代理读取目标特定的属性值
	 * <p>
	 * 实现IDelegateInvoker接口的方法，通过指定的函数式方法读取目标对象的特定属性值，
	 * 并将读取到的值传递给消费者函数进行处理。将任务封装后添加到任务队列中，并支持链式调用返回当前执行器实例。
	 *
	 * @param method   用于读取目标对象属性的函数式方法，不能为null
	 * @param consumer 用于处理读取到的属性值的消费者函数，不能为null
	 * @param <P>      属性值的类型
	 * @return 当前代理执行器实例，用于链式调用
	 * @throws IllegalArgumentException 当method或consumer为null时抛出
	 * @see IDelegateInvoker#takeOut(SFunction, SConsumer) 代理执行器接口方法
	 * @see SFunction 函数式接口
	 * @see SConsumer 消费者函数式接口
	 * @see AssertUtil 断言工具类
	 */
	@Override
	public <P> IDelegateInvoker<T> takeOut(SFunction<T, P> method, SConsumer<P> consumer) {
		AssertUtil.notNull(method, I18nUtil.get(CommonI18nConstant.DELEGATE_METHOD_REQUIRED));
		AssertUtil.notNull(consumer, I18nUtil.get(CommonI18nConstant.DELEGATE_CONSUMER_REQUIRED));
		tasks.add(new InvokeTask<>(method, consumer, index.addAndGet(1), false));
		return this;
	}

	/**
	 * 执行委托写入方法
	 * <p>
	 * 抽象方法，由子类实现，用于执行具体的代理写入操作。根据传入的参数对目标对象进行属性写入操作， 并在执行过程中处理可能发生的异常情况。
	 *
	 * @param flag   代理执行标记，用于跟踪执行状态
	 * @param method 用于写入目标对象属性的双参数消费者函数
	 * @param p      用于提供写入值的供应者函数
	 * @param <P>    属性值的类型
	 * @see IDelegateFlag 代理执行标记接口
	 * @see SBiConsumer 双参数消费者函数式接口
	 * @see SSupplier 供应者函数式接口
	 */
	protected abstract <P> void executeTakeIn(IDelegateFlag<T> flag, SBiConsumer<T, P> method, SSupplier<P> p);

	/**
	 * 执行委托读取方法
	 * <p>
	 * 抽象方法，由子类实现，用于执行具体的代理读取操作。根据传入的参数从目标对象读取属性值，
	 * 并将结果传递给消费者函数进行处理，在执行过程中处理可能发生的异常情况。
	 *
	 * @param flag     代理执行标记，用于跟踪执行状态
	 * @param method   用于读取目标对象属性的函数式方法
	 * @param consumer 用于处理读取到的属性值的消费者函数
	 * @param <P>      属性值的类型
	 * @see IDelegateFlag 代理执行标记接口
	 * @see SFunction 函数式接口
	 * @see SConsumer 消费者函数式接口
	 */
	protected abstract <P> void executeTakeOut(IDelegateFlag<T> flag, SFunction<T, P> method, SConsumer<P> consumer);

	/**
	 * 错误处理方法
	 * <p>
	 * 当代理任务执行过程中发生异常时调用此方法进行统一错误处理， 将异常信息设置到代理执行标记中，以便调用方能够获取和处理异常情况。
	 *
	 * @param flag 代理执行标记，用于设置异常信息
	 * @param e    执行过程中发生的异常
	 * @see IDelegateFlag 代理执行标记接口
	 */
	protected void onErrorThrow(IDelegateFlag<T> flag, Throwable e) {
		flag.setException(e, this);
	}

	/**
	 * 执行单次代理任务
	 * <p>
	 * 实现IDelegateInvoker接口的方法，从任务队列中取出一个任务并执行，
	 * 返回执行结果标记用于跟踪和管理代理任务的执行状态和结果。如果任务队列为空，则直接标记为完成状态。
	 *
	 * @return 代理执行结果标记
	 * @see IDelegateInvoker#executeOnce() 代理执行器接口方法
	 * @see IDelegateFlag 代理执行标记接口
	 * @see IDelegateFlagProvider 代理执行标记提供者接口
	 */
	@Override
	public IDelegateFlag<T> executeOnce() {
		IDelegateFlagProvider provider = getDelegateFlagProvider();
		IDelegateFlag<T> flag = provider.getDelegateFlag(this, getTarget());
		if (tasks.isEmpty()) {
			flag.setFinished(this);
			index.addAndGet(-1);
			return flag;
		}
		InvokeTask task = tasks.poll();
		if (task.isIn) {
			executeTakeIn(flag, (SBiConsumer<T, Object>) task.in, (SSupplier<Object>) task.out);
		} else {
			executeTakeOut(flag, (SFunction<T, Object>) task.in, (SConsumer<Object>) task.out);
		}

		return flag;
	}

	/**
	 * 执行所有代理任务
	 * <p>
	 * 实现IDelegateInvoker接口的方法，执行任务队列中的所有代理任务， 并返回执行结果标记集合，用于批量处理和跟踪多个代理任务的执行状态和结果。
	 *
	 * @return 代理执行结果标记集合
	 * @see IDelegateInvoker#executeAll() 代理执行器接口方法
	 * @see IDelegateFlag 代理执行标记接口
	 * @see Collection 集合接口
	 */
	@Override
	public Collection<IDelegateFlag<T>> executeAll() {
		List<IDelegateFlag<T>> flags = new ArrayList<>(waittingCount() + 4);
		while (!tasks.isEmpty()) {
			IDelegateFlag<T> flag = executeOnce();
			flags.add(flag);
		}
		return flags;
	}

	/**
	 * 获取等待执行完成的任务数量
	 * <p>
	 * 实现IDelegateInvoker接口的方法，返回当前正在等待执行完成的代理任务数量， 用于监控和管理代理任务的执行进度。实际返回任务队列中的任务数量。
	 *
	 * @return 等待执行完成的任务数量
	 * @see IDelegateInvoker#waittingCount() 代理执行器接口方法
	 * @see Queue#size() 队列大小方法
	 */
	@Override
	public int waittingCount() {
		return tasks.size();
	}
}
