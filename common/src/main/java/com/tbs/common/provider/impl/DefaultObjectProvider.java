package com.tbs.common.provider.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.tbs.common.provider.IObjectProvider;
import com.tbs.common.provider.IObjectResolver;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.ObjectResolveContext;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.provider.ResolvedObject;
import com.tbs.common.provider.RoutingStrategy;
import com.tbs.common.provider.impl.support.RoutingServiceProxy;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一对象提供器默认实现
 * <p>
 * 过渡期门面：{@code getObject}/{@code getObjects}/{@code getResolvedObject(按来源)} 仍走旧解析链
 * （逐个询问 {@link IObjectResolver}），供阶段 3-δ 切换过程中与新中心链路并列观察对比；
 * 路由代理构建已改派 {@link IRegistryCenter#getRoutingProxy}，由中心按 business/version 择一路由目标（Dubbo 为集群引用）。
 * 单个解析器抛异常时仅跳过该来源并记录日志，不影响其余来源解析。
 * 路由代理自身由各来源解析器按 {@link RoutingServiceProxy#isRoutingProxy(Object)} 排除，
 * 因为代理是「通道」而非实现，否则代理转发时会再次解析到自己造成无限递归。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class DefaultObjectProvider implements IObjectProvider {

    /**
     * 有序解析链（旧链，仅服务于尚未迁移到中心的单实例/枚举/按来源解析）
     */
    private final List<IObjectResolver> resolvers;

    /**
     * 来源到解析器的映射，同一来源多个解析器时保留优先级最高的一个
     */
    private final Map<ObjectSource, IObjectResolver> resolversBySource;

    /**
     * 统一注册中心，路由代理构建委派于此
     */
    private final IRegistryCenter registryCenter;

    /**
     * 构造统一对象提供器
     *
     * @param resolvers      解析器集合，不能为null，内部按 Ordered 排序
     * @param registryCenter 统一注册中心，不能为null
     */
    public DefaultObjectProvider(List<IObjectResolver> resolvers, IRegistryCenter registryCenter) {
        var sorted = new ArrayList<>(resolvers);
        sorted.sort(Comparator.comparingInt(IObjectResolver::getOrder));
        this.resolvers = sorted;
        this.registryCenter = registryCenter;
        this.resolversBySource = new HashMap<>();
        for (IObjectResolver resolver : sorted) {
            if (resolver.getSource() != null) {
                this.resolversBySource.putIfAbsent(resolver.getSource(), resolver);
            }
        }
        log.debug("[ObjectProvider] 多来源解析链初始化: {}",
            sorted.stream().map(r -> r.getSource() + "(order=" + r.getOrder() + ")")
                .collect(java.util.stream.Collectors.joining(" -> ")));
    }

    /**
     * 链式解析单个实例
     * <p>
     * 依次询问解析链，各解析器自行返回自身语义下的最优实例（如 Dubbo 的负载均衡整体引用）；
     * 全部来源未命中返回 empty。
     * </p>
     *
     * @param context 解析上下文，不能为null
     * @param <T>     目标类型
     * @return 首个命中的解析结果
     */
    private <T> Optional<ResolvedObject<T>> resolveFirst(ObjectResolveContext<T> context) {
        for (IObjectResolver resolver : resolvers) {
            try {
                var resolved = resolver.resolveOne(context);
                if (resolved.isPresent()) {
                    log.debug("[ObjectProvider] 单实例解析命中, 接口: {}, 来源: {}, 实例: {}",
                        context.getTargetType().getName(), resolved.get().getSource(), resolved.get().getInstanceKey());
                    return resolved;
                }
            } catch (Exception e) {
                log.warn("解析器[{}]解析失败, type: {}, 原因: {}", resolver.getClass().getSimpleName(),
                    context.getTargetType().getName(), e.getMessage());
            }
        }
        log.debug("[ObjectProvider] 全部来源均未命中, 接口: {}, beanName: {}", context.getTargetType().getName(),
            context.getBeanName());
        return Optional.empty();
    }

    @Override
    public <T> T getObject(Class<T> type) {
        return getObject(type, null);
    }

    @Override
    public <T> T getObject(Class<T> type, String beanName) {
        return resolveFirst(ObjectResolveContext.of(type, beanName)).map(ResolvedObject::getInstance).orElse(null);
    }

    @Override
    public <T> Optional<T> getObjectOptional(Class<T> type) {
        return Optional.ofNullable(getObject(type, null));
    }

    @Override
    public <T> Optional<T> getObjectOptional(Class<T> type, String beanName) {
        return Optional.ofNullable(getObject(type, beanName));
    }

    @Override
    public <T> Collection<ResolvedObject<T>> getObjects(Class<T> type) {
        List<ResolvedObject<T>> result = new ArrayList<>();
        var context = ObjectResolveContext.of(type);
        for (IObjectResolver resolver : resolvers) {
            try {
                resolver.resolveAll(context).forEach(result::add);
            } catch (Exception e) {
                log.warn("解析器[{}]枚举失败, type: {}, 原因: {}", resolver.getClass().getSimpleName(), type.getName(),
                    e.getMessage());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("[ObjectProvider] 多来源枚举接口: {}, 共 {} 个实例: {}", type.getName(), result.size(),
                result.stream().map(o -> o.getSource() + ":" + o.getInstanceKey())
                    .collect(java.util.stream.Collectors.toList()));
        }
        return result;
    }

    @Override
    public <T> Optional<ResolvedObject<T>> getResolvedObject(Class<T> type, ObjectSource source) {
        var resolver = resolversBySource.get(source);
        if (resolver == null) {
            return Optional.empty();
        }
        try {
            return resolver.resolveOne(ObjectResolveContext.of(type));
        } catch (Exception e) {
            log.warn("解析器[{}]按来源[{}]解析失败, type: {}, 原因: {}", resolver.getClass().getSimpleName(), source,
                type.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 构建多来源路由代理
     * <p>
     * 阶段 3-δ 起委派统一注册中心构建与缓存：代理每次调用经 {@code center.resolveFirst} 按策略与 business/version
     * 择一路由目标（Dubbo 命中坐标级集群引用、保留负载均衡/failover）。本门面不指定 business/version，传 null 不约束。
     * </p>
     *
     * @param interfaceType 目标接口类型，不能为null
     * @param strategy      路由策略，不能为null
     * @param <T>           目标接口类型
     * @return 路由代理实例
     */
    @Override
    public <T> T getRoutingProxy(Class<T> interfaceType, RoutingStrategy strategy) {
        return registryCenter.getRoutingProxy(interfaceType, strategy, null, null);
    }
}
