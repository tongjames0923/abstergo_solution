package com.tbs.cache.cascade;

import java.util.Collection;
import java.util.Collections;

/**
 * 关联缓存失效声明
 * <p>
 * 声明「某个缓存被清除时，另一个缓存要连带清除」。由框架内置的
 * {@link CascadeEvictCacheListenner} 在清除完成后统一执行，业务写入点不需要再手工逐个清除，
 * 因此不会因为新增写入点而漏掉失效。
 * </p>
 * <p>
 * <b>一条声明只描述一条边：一个源对应一个目标。</b>原因是指键映射的作域只能属于一条边：
 * 若一条声明挂多个目标，目标们会被迫共用同一份 {@link #translate(Object)} 结果，
 * 一旦其中某个目标需要换键、另一个需要原键，就会把错误的键套到两者身上，造成**静默漏清**。
 * 一个源要连带多个缓存时，写多条声明（各自一个目标、各自一个键映射）。
 * </p>
 * <p>
 * 实现方放在业务模块内（因为只有业务知道表关系），由 Spring 容器收集，Bean 即注册。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ICacheEvictLink {

    /**
     * 获取声明名称
     * <p>
     * 仅用于日志与启动校验定位，须能唯一标识一条声明。
     * </p>
     *
     * @return 声明名称，不能为空
     */
    String getName();

    /**
     * 被清除的源缓存名称
     *
     * @return 缓存名称，不能为空，取值应为 {@code CacheNamesConstants} 常量
     */
    String source();

    /**
     * 需要连带清除的目标缓存名称
     * <p>
     * 只能是一个。连带多个目标请写多条声明，理由见类注释。
     * </p>
     *
     * @return 目标缓存名称，取值应为 {@code CacheNamesConstants} 常量
     */
    String target();

    /**
     * 把源缓存的键映射为目标缓存的键
     * <p>
     * 默认原样传递，适用于源与目标共用一个键空间的场景（如都是按 userId 缓存的派生视图）。
     * 当源与目标键维度不同时（如源按 groupId 缓存、目标按 userId 缓存），由实现方覆写本方法反查
     * 目标键集合——这里允许查库，因为实现类在业务模块内，框架只是回调它，框架自身不含任何表知识。
     * </p>
     * <p>
     * 本方法是跨键失效的<b>唯一</b>扩展口子，不提供其他注入点。实现须为只读、幂等，
     * 抛出的异常由级联执行方捕获记录，不影响业务写入，此时派生缓存依赖各自的 TTL 达到最终一致。
     * </p>
     *
     * @param sourceKey 被清除的源缓存键
     * @return 需要连带清除的目标键集合，返回空集合表示无需连带
     */
    default Collection<Object> translate(Object sourceKey) {
        return Collections.singletonList(sourceKey);
    }
}
