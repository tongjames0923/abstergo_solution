package com.tbs.transaction.scalable.impl;

import java.util.Optional;

import javax.annotation.Resource;

import com.tbs.common.util.framework.SpringUtil;
import com.tbs.lock.ILockProvider;
import com.tbs.transaction.scalable.*;

/**
 * 基于Spring管理的可扩展事务实现类
 * <p>
 * 该类是{@link AbstractScalableTransaction}的实现，通过Spring框架管理依赖注入。
 * 它使用Spring的@Resource注解注入必要的依赖项，并通过SpringUtil获取可选的组件。
 * </p>
 *
 * @see AbstractScalableTransaction 可扩展事务抽象类
 * @see ILockProvider 分布式锁提供者接口
 * @see ITransactionStore 事务存储接口
 * @see ITransactionCreator 事务创建器接口
 * @see ITransactionStatusChangeLisenner 事务状态变更监听器接口
 * @see ITransactionConflictSolver 事务冲突解决器接口
 * @see ITransactionEdgeController 事务边界控制器接口
 */
public class SpringManagedScalableTransaction extends AbstractScalableTransaction {

	/**
	 * 分布式锁提供者，用于获取分布式锁实例
	 *
	 * @see ILockProvider#getLocker(Object) 获取锁实例方法
	 */
	@Resource
	private ILockProvider lockProvider;

	/**
	 * 事务存储器，用于存储和获取事务信息
	 *
	 * @see ITransactionStore#store(String, com.tbs.transaction.ITransactionInfo)
	 *      存储事务信息方法
	 * @see ITransactionStore#get(String) 获取事务信息方法
	 */
	@Resource
	private ITransactionStore transactionStore;

	/**
	 * 事务创建器，用于创建新的事务信息对象
	 *
	 * @see ITransactionCreator#createTransactionInfo(String) 创建事务信息方法
	 */
	@Resource
	private ITransactionCreator transactionCreator;

	/**
	 * 获取锁提供者实例
	 * <p>
	 * 该方法返回通过Spring注入的锁提供者实例。
	 * </p>
	 *
	 * @return ILockProvider 锁提供者实例
	 * @see AbstractScalableTransaction#getLockProvider()
	 * @see ILockProvider 分布式锁提供者接口
	 */
	@Override
	protected ILockProvider getLockProvider() {
		return lockProvider;
	}

	/**
	 * 获取事务存储实例
	 * <p>
	 * 该方法返回通过Spring注入的事务存储实例。
	 * </p>
	 *
	 * @return ITransactionStore 事务存储实例
	 * @see AbstractScalableTransaction#getTransactionStore()
	 * @see ITransactionStore 事务存储接口
	 */
	@Override
	protected ITransactionStore getTransactionStore() {
		return transactionStore;
	}

	/**
	 * 获取事务创建器实例
	 * <p>
	 * 该方法返回通过Spring注入的事务创建器实例。
	 * </p>
	 *
	 * @return ITransactionCreator 事务创建器实例
	 * @see AbstractScalableTransaction#getTransactionCreator()
	 * @see ITransactionCreator 事务创建器接口
	 */
	@Override
	protected ITransactionCreator getTransactionCreator() {
		return transactionCreator;
	}

	/**
	 * 获取事务状态改变监听器实例
	 * <p>
	 * 该方法通过SpringUtil从Spring容器中获取事务状态改变监听器的可选实例。
	 * 如果Spring容器中没有定义该类型的Bean，则返回空Optional。
	 * </p>
	 *
	 * @return Optional<ITransactionStatusChangeLisenner> 事务状态改变监听器的可选实例
	 * @see AbstractScalableTransaction#getTransactionStatusChangeLisenner()
	 * @see ITransactionStatusChangeLisenner 事务状态变更监听器接口
	 * @see SpringUtil#getBeanOptional(Class) 从Spring容器获取Bean的可选实例
	 */
	@Override
	protected Optional<ITransactionStatusChangeLisenner> getTransactionStatusChangeLisenner() {
		return SpringUtil.getBeanOptional(ITransactionStatusChangeLisenner.class);
	}

	/**
	 * 获取事务冲突解决器实例
	 * <p>
	 * 该方法通过SpringUtil从Spring容器中获取事务冲突解决器的可选实例。
	 * 如果Spring容器中没有定义该类型的Bean，则返回空Optional。
	 * </p>
	 *
	 * @return Optional<ITransactionConflictSolver> 事务冲突解决器的可选实例
	 * @see AbstractScalableTransaction#getTransactionConflictSolver()
	 * @see ITransactionConflictSolver 事务冲突解决器接口
	 * @see SpringUtil#getBeanOptional(Class) 从Spring容器获取Bean的可选实例
	 */
	@Override
	protected Optional<ITransactionConflictSolver> getTransactionConflictSolver() {
		return SpringUtil.getBeanOptional(ITransactionConflictSolver.class);
	}

	/**
	 * 获取事务边界控制器实例
	 * <p>
	 * 该方法通过SpringUtil从Spring容器中获取事务边界控制器的可选实例。
	 * 如果Spring容器中没有定义该类型的Bean，则返回空Optional。
	 * </p>
	 *
	 * @return Optional<ITransactionEdgeController> 事务边界控制器的可选实例
	 * @see AbstractScalableTransaction#getTransactionEdgeController()
	 * @see ITransactionEdgeController 事务边界控制器接口
	 * @see SpringUtil#getBeanOptional(Class) 从Spring容器获取Bean的可选实例
	 */
	@Override
	protected Optional<ITransactionEdgeController> getTransactionEdgeController() {
		return SpringUtil.getBeanOptional(ITransactionEdgeController.class);
	}
}
