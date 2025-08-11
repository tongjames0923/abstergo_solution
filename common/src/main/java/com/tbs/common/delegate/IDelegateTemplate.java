package com.tbs.common.delegate;

/**
 * 代理模板接口
 * <p>
 * 该接口定义了代理模式的核心功能模板，提供了创建代理目标对象和代理调用者的标准方法。
 * 通过该模板可以统一管理代理对象的创建过程，实现对目标对象的间接访问和控制。 支持泛型操作，可以处理任意类型的对象代理需求。
 *
 * @author tongj
 * @see IDelegateTarget 代理目标接口
 * @see IDelegateInvoker 代理调用者接口
 */
public interface IDelegateTemplate {

	/**
	 * 创建代理目标对象
	 * <p>
	 * 根据指定的目标对象创建对应的代理目标实例，用于包装和管理目标对象的访问。 代理目标对象提供了对原始对象的读取和写入操作的统一接口。
	 *
	 * @param targetObject 要被代理的目标对象，不能为null
	 * @param <T>          目标对象的类型
	 * @return 代理目标对象实例
	 * @see IDelegateTarget 代理目标接口
	 */
	<T> IDelegateTarget<T> createDelegateTarget(T targetObject);

	/**
	 * 创建代理调用者
	 * <p>
	 * 根据指定的调用者类型和代理目标对象创建对应的代理调用者实例，用于执行对目标对象的代理操作。 代理调用者提供了对目标对象属性的读取、写入以及方法执行等操作。
	 *
	 * @param invokerType    代理调用者类型，必须是IDelegateInvoker的实现类，不能为null
	 * @param delegateTarget 代理目标对象，不能为null
	 * @param <T>            目标对象的类型
	 * @return 代理调用者实例
	 * @see IDelegateInvoker 代理调用者接口
	 * @see IDelegateTarget 代理目标接口
	 * @see Class 类对象
	 */
	<T> IDelegateInvoker<T> createDelegateInvoker(Class<? extends IDelegateInvoker> invokerType,
			IDelegateTarget<T> delegateTarget);

}
