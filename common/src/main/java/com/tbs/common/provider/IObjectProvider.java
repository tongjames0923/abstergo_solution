package com.tbs.common.provider;

import java.util.Collection;
import java.util.Optional;

/**
 * 统一对象提供器门面
 * <p>
 * 提供多来源（Spring 容器 + Dubbo 注册中心）对象实例的统一访问与枚举能力：
 * 按类型获取实例、按类型枚举全部来源的全部可用实现（携带来源与实例标识元数据）。
 * 本门面统一委派 {@link IRegistryCenter}，与路由代理共用同一份来源数据，两条读取路径结果一致。
 * 实例不存在或来源不可用时统一返回空集合/空 Optional/null，由调用方自行判断，不强制抛出业务异常。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 * @see IObjectResolver 来源解析器 SPI
 */
public interface IObjectProvider {

    /**
     * 按类型获取单个实例（解析链首个命中）
     *
     * @param type 目标类型，不能为null
     * @param <T>  目标类型
     * @return 实例，无任何来源可用时返回null
     */
    <T> T getObject(Class<T> type);

    /**
     * 按类型与 Bean 名称获取单个实例（解析链首个命中）
     *
     * @param type     目标类型，不能为null
     * @param beanName 精确 Bean 名称，可为空表示按类型解析
     * @param <T>      目标类型
     * @return 实例，无任何来源可用时返回null
     */
    <T> T getObject(Class<T> type, String beanName);

    /**
     * 按类型获取单个实例（Optional 语义）
     *
     * @param type 目标类型，不能为null
     * @param <T>  目标类型
     * @return Optional 包装的实例，无任何来源可用时为 Optional.empty()
     */
    <T> Optional<T> getObjectOptional(Class<T> type);

    /**
     * 按类型与 Bean 名称获取单个实例（Optional 语义）
     *
     * @param type     目标类型，不能为null
     * @param beanName 精确 Bean 名称，可为空表示按类型解析
     * @param <T>      目标类型
     * @return Optional 包装的实例，无任何来源可用时为 Optional.empty()
     */
    <T> Optional<T> getObjectOptional(Class<T> type, String beanName);

    /**
     * 枚举当前上下文中目标类型的全部可用实现
     * <p>
     * 枚举该接口在中心的全部 {@code enumerable} 条目，按本地优先排序：容器内全部同类型 Bean（Spring 来源，含 @DubboReference 引用 Bean）
     * 与 Dubbo 注册中心发现的全部服务提供者实例（Dubbo 来源，逐实例直连）依次排列，每个结果携带来源与实例标识。
     * 路由代理作为「通道」而非实现，不会出现在枚举结果中。
     * </p>
     *
     * @param type 目标类型，不能为null
     * @param <T>  目标类型
     * @return 全部可用实现集合，无可用实现时为空集合，不会为null
     */
    <T> Collection<ResolvedObject<T>> getObjects(Class<T> type);

    /**
     * 从指定来源解析单个最优实例
     * <p>
     * 只取该来源的条目，不会混入其余来源；Spring 来源返回本地 Bean，Dubbo 来源返回负载均衡后的整体引用。
     * 来源无对应解析器或未命中时返回 {@link Optional#empty()}，不抛异常。
     * </p>
     *
     * @param type   目标类型，不能为null
     * @param source 目标来源，不能为null
     * @param <T>    目标类型
     * @return 该来源的最优实例，未命中时为 {@link Optional#empty()}
     */
    <T> Optional<ResolvedObject<T>> getResolvedObject(Class<T> type, ObjectSource source);
}
