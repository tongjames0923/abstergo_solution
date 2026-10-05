package com.tbs.common.provider;

import java.util.Collection;
import java.util.Optional;

import org.springframework.core.Ordered;

/**
 * 对象实例解析器 SPI
 * <p>
 * 定义多来源实例解析的扩展契约：给定解析上下文，返回当前来源下全部可用的实例集合。
 * 每个解析器只负责自己管辖的单一来源（如 Spring 容器、Dubbo 注册中心）并以 {@link #getSource()}
 * 声明该来源，供门面按来源定向解析；来源扩展只需新增实现，
 * 聚合门面 {@link IObjectProvider} 会按 {@link Ordered} 顺序依次询问全部解析器。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 * @see SpringObjectResolver Spring 容器解析器
 * @see IObjectProvider 聚合门面
 */
public interface IObjectResolver extends Ordered {

    /**
     * 解析当前来源下目标类型的全部可用实例
     * <p>
     * 无可用实例时应返回空集合而非null；解析过程中的内部失败（如来源不可用）应记录日志并降级为空集合，禁止向调用方抛出异常。
     * </p>
     *
     * @param context 解析上下文，不能为null
     * @param <T>     目标实例类型
     * @return 带来源元数据的全部可用实例，不会为null
     */
    <T> Collection<ResolvedObject<T>> resolveAll(ObjectResolveContext<T> context);

    /**
     * 当前解析器管辖的对象来源
     * <p>
     * 供门面按来源定向解析（路由代理只在策略接受的来源内取实例，不做全链枚举后筛选）。
     * 同一来源存在多个解析器时，按 {@link Ordered} 优先级取第一个。
     * </p>
     *
     * @return 对象来源，不能为null
     */
    ObjectSource getSource();

    /**
     * 解析当前来源下目标类型的单个最优实例
     * <p>
     * 默认实现取 {@link #resolveAll(ObjectResolveContext)} 的首个结果；解析器可按自身语义覆写（如返回负载均衡后的整体引用）。
     * 返回的实例必须与解析上下文指定的实例标识匹配。
     * </p>
     *
     * @param context 解析上下文，不能为null
     * @param <T>     目标实例类型
     * @return 单个可用实例，来源无可用实例时返回 Optional.empty()
     */
    default <T> Optional<ResolvedObject<T>> resolveOne(ObjectResolveContext<T> context) {
        var all = resolveAll(context);
        return all.isEmpty() ? Optional.empty() : Optional.of(all.iterator().next());
    }
}
