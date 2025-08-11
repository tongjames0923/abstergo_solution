package com.tbs.common.delegate.provider;

import com.tbs.common.delegate.IDelegateTarget;

/**
 * 代理对象提供者接口
 * <p>
 * 该接口定义了代理目标对象的创建和提供规范，用于统一管理代理目标对象的生成过程。
 * 通过该提供者可以将普通对象包装为代理目标对象，实现对目标对象的间接访问和控制。
 * 支持泛型操作，可以处理任意类型的对象代理需求。
 *
 * @author tbs
 * @see IDelegateTarget 代理目标接口
 */
public interface IDelegateTargetProvider {
    /**
     * 获取代理对象
     * <p>
     * 根据指定的目标对象创建并返回对应的代理目标对象实例，用于包装和管理目标对象的访问。
     * 代理目标对象提供了对原始对象的读取和写入操作的统一接口，实现对目标对象的安全访问。
     *
     * @param targetObject 要被代理的目标对象，不能为null
     * @param <T>          目标对象的类型
     * @return 代理目标对象实例
     * @see IDelegateTarget 代理目标接口
     */
    <T> IDelegateTarget<T> getDelegateTarget(T targetObject);
}
