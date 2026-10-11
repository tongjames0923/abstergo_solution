package com.tbs.common.delegate.impl;

import jakarta.annotation.Resource;

import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.delegate.IDelegateTarget;
import com.tbs.common.delegate.IDelegateTemplate;
import com.tbs.common.delegate.provider.IDelegateInvokerProvider;
import com.tbs.common.delegate.provider.IDelegateTargetProvider;

/**
 * Spring托管的代理模板实现类
 * <p>
 * 该类是IDelegateTemplate接口的实现，基于Spring框架的依赖注入机制管理代理组件的创建和配置。
 * 通过注入的代理目标提供者和代理执行器提供者，统一创建和管理代理目标对象和代理执行器实例， 实现了代理模式的标准化操作流程，简化了代理对象的使用过程。
 *
 * @author tongj
 * @see IDelegateTemplate 代理模板接口
 * @see IDelegateTargetProvider 代理目标提供者接口
 * @see IDelegateInvokerProvider 代理执行器提供者接口
 * @see IDelegateTarget 代理目标接口
 * @see IDelegateInvoker 代理执行器接口
 */
public class SpringManagedDelegateTemplate implements IDelegateTemplate {

	/**
	 * 代理目标提供者
	 * <p>
	 * 通过Spring的@Resource注解自动注入，用于创建和管理代理目标对象实例。
	 *
	 * @see IDelegateTargetProvider 代理目标提供者接口
	 * @see Resource 资源注入注解
	 */
	@Resource
	private IDelegateTargetProvider provider;

	/**
	 * 代理执行器提供者
	 * <p>
	 * 通过Spring的@Resource注解自动注入，用于创建和管理代理执行器实例。
	 *
	 * @see IDelegateInvokerProvider 代理执行器提供者接口
	 * @see Resource 资源注入注解
	 */
	@Resource
	private IDelegateInvokerProvider invokerProvider;

	/**
	 * 创建代理目标对象
	 * <p>
	 * 实现IDelegateTemplate接口的方法，根据指定的目标对象创建对应的代理目标实例。
	 * 通过注入的代理目标提供者完成实际的创建过程，用于包装和管理目标对象的访问。
	 *
	 * @param targetObject 要被代理的目标对象，不能为null
	 * @param <T>          目标对象的类型
	 * @return 代理目标对象实例
	 * @see IDelegateTemplate#createDelegateTarget(Object) 代理模板接口方法
	 * @see IDelegateTargetProvider#getDelegateTarget(Object) 代理目标提供者方法
	 */
	@Override
	public <T> IDelegateTarget<T> createDelegateTarget(T targetObject) {
		return provider.getDelegateTarget(targetObject);
	}

	/**
	 * 创建代理执行器
	 * <p>
	 * 实现IDelegateTemplate接口的方法，根据指定的执行器类型和代理目标对象创建对应的代理执行器实例。
	 * 通过注入的代理执行器提供者完成实际的创建过程，并自动设置代理目标对象，返回已准备就绪的代理执行器实例。
	 *
	 * @param invokerType    代理执行器的类型，必须是IDelegateInvoker的实现类，不能为null
	 * @param delegateTarget 代理目标对象，不能为null
	 * @param <T>            目标对象的类型
	 * @return 已设置目标对象的代理执行器实例
	 * @see IDelegateTemplate#createDelegateInvoker(Class, IDelegateTarget) 代理模板接口方法
	 * @see IDelegateInvokerProvider#makeInvoker(Class) 代理执行器提供者方法
	 * @see IDelegateInvoker#setTarget(IDelegateTarget) 代理执行器设置目标方法
	 */
	@Override
	public <T> IDelegateInvoker<T> createDelegateInvoker(Class<? extends IDelegateInvoker> invokerType,
			IDelegateTarget<T> delegateTarget) {
		IDelegateInvoker<T> invoker = invokerProvider.makeInvoker(invokerType);
		invoker.setTarget(delegateTarget);
		return invoker;
	}
}
