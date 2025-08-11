package com.tbs.common.delegate.provider;

import com.tbs.common.delegate.IDelegateFlag;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.delegate.IDelegateTarget;

/**
 * 代理执行标记提供者接口
 * <p>
 * 该接口定义了代理执行标记的创建和提供规范，用于统一管理代理执行标记的生成过程。
 * 通过该提供者可以创建与特定执行器和目标对象关联的代理执行标记实例，
 * 实现对代理任务执行状态的跟踪和管理。
 *
 * @author tongj
 * @see IDelegateFlag 代理执行标记接口
 * @see IDelegateInvoker 代理执行器接口
 * @see IDelegateTarget 代理目标接口
 */
public interface IDelegateFlagProvider {

    /**
     * 获取代理执行标记
     * <p>
     * 根据指定的代理执行器和代理目标对象创建并返回对应的代理执行标记实例。
     * 创建的标记用于跟踪和管理特定代理任务的执行状态和结果，包括正常完成和异常情况的处理。
     *
     * @param invoker        代理执行器实例，不能为null
     * @param delegateTarget 代理目标对象实例，不能为null
     * @param <T>            目标对象的类型
     * @return 与指定执行器和目标对象关联的代理执行标记实例
     * @see IDelegateFlag 代理执行标记接口
     * @see IDelegateInvoker 代理执行器接口
     * @see IDelegateTarget 代理目标接口
     */
    <T> IDelegateFlag<T> getDelegateFlag(IDelegateInvoker<T> invoker, IDelegateTarget<T> delegateTarget);
}
