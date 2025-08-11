package com.tbs.common.delegate.provider;

import com.tbs.common.delegate.IDelegateInvoker;

/**
 * 代理执行器提供者接口
 * <p>
 * 该接口定义了代理执行器的创建和提供规范，用于统一管理代理执行器的生成过程。
 * 通过该提供者可以创建指定类型的代理执行器实例，实现对代理执行器的统一管理和配置。
 *
 * @author tongj
 * @see IDelegateInvoker 代理执行器接口
 */
public interface IDelegateInvokerProvider {

    /**
     * 创建一个代理执行器
     * <p>
     * 根据指定的执行器类型创建并返回对应的代理执行器实例。创建的执行器可用于执行对目标对象的代理操作，
     * 包括属性读写、方法调用等。创建后的执行器需要手动设置目标对象才能使用。
     *
     * @param invokerType 代理执行器的类型，必须是IDelegateInvoker的实现类，不能为null
     * @param <T>         目标对象的类型
     * @return 指定类型的代理执行器实例
     * @see IDelegateInvoker 代理执行器接口
     * @see Class 类对象
     */
    <T> IDelegateInvoker<T> makeInvoker(Class<? extends IDelegateInvoker> invokerType);
}
