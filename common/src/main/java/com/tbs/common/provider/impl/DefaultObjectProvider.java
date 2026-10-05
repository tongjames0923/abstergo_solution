package com.tbs.common.provider.impl;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.tbs.common.provider.IObjectProvider;
import com.tbs.common.provider.IObjectResolver;
import com.tbs.common.provider.ObjectResolveContext;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.provider.ResolvedObject;
import com.tbs.common.provider.RoutingStrategy;
import com.tbs.common.provider.impl.support.RoutingServiceProxy;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一对象提供器默认实现
 * <p>
 * 聚合全部 {@link IObjectResolver}（按 Ordered 排序）形成解析链：
 * 单实例解析逐个询问解析器的 {@link IObjectResolver#resolveOne(ObjectResolveContext)} 取首个命中来源的最优实例，
 * 按来源解析只访问该来源对应的解析器，getObjects 聚合全部来源结果实现多实现枚举；
 * getRoutingProxy 构建并缓存多来源路由代理，供依赖注入场景使用。
 * 单个解析器抛出异常时仅跳过该来源并记录日志，不影响其余来源解析。
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
     * 有序解析链
     */
    private final List<IObjectResolver> resolvers;

    /**
     * 来源到解析器的映射，同一来源多个解析器时保留优先级最高的一个
     */
    private final Map<ObjectSource, IObjectResolver> resolversBySource;

    /**
     * 路由代理缓存，键为 接口全限定名 + 路由策略
     */
    private final ConcurrentMap<String, Object> proxyCache = new ConcurrentHashMap<>();

    /**
     * 构造统一对象提供器
     *
     * @param resolvers 解析器集合，不能为null，内部按 Ordered 排序
     */
    public DefaultObjectProvider(List<IObjectResolver> resolvers) {
        var sorted = new ArrayList<>(resolvers);
        sorted.sort(Comparator.comparingInt(IObjectResolver::getOrder));
        this.resolvers = sorted;
        this.resolversBySource = new EnumMap<>(ObjectSource.class);
        for (IObjectResolver resolver : sorted) {
            if (resolver.getSource() != null) {
                this.resolversBySource.putIfAbsent(resolver.getSource(), resolver);
            }
        }
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
                    return resolved;
                }
            } catch (Exception e) {
                log.warn("解析器[{}]解析失败, type: {}, 原因: {}", resolver.getClass().getSimpleName(),
                    context.getTargetType().getName(), e.getMessage());
            }
        }
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
     * 构建或获取缓存的路由代理
     * <p>
     * 代理缓存键为接口全限定名与路由策略组合，同接口同策略复用同一代理实例。
     * </p>
     *
     * @param interfaceType 目标接口类型，不能为null
     * @param strategy      路由策略，不能为null
     * @param <T>           目标接口类型
     * @return 路由代理实例
     */
    @SuppressWarnings("unchecked")
    private <T> T getOrCreateProxy(Class<T> interfaceType, RoutingStrategy strategy) {
        var cacheKey = interfaceType.getName() + "#" + strategy.name();
        return (T) proxyCache.computeIfAbsent(cacheKey,
            key -> Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class<?>[] {interfaceType},
                new RoutingServiceProxy<>(interfaceType, strategy, this)));
    }

    @Override
    public <T> T getRoutingProxy(Class<T> interfaceType, RoutingStrategy strategy) {
        return getOrCreateProxy(interfaceType, strategy);
    }
}
