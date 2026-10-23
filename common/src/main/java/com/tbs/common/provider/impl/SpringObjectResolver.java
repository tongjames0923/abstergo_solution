package com.tbs.common.provider.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.ClassUtils;

import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.provider.IObjectResolver;
import com.tbs.common.provider.IRouteMeta;
import com.tbs.common.provider.ObjectResolveContext;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.provider.ResolvedObject;
import com.tbs.common.provider.SourceCoordinate;
import com.tbs.common.provider.impl.support.RoutingServiceProxy;
import com.tbs.common.util.framework.SpringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring 容器对象解析器
 * <p>
 * 多来源解析链中的最高优先级解析器，委托 {@link SpringUtil} 从 Spring 容器解析实例：
 * 容器内普通 Bean 与经 @DubboReference 注册进容器的引用 Bean 均由本来源覆盖。
 * 路由代理以接口类型注册进容器后同样会被 getBeansOfType 枚举到，但它是转发通道而非实现，
 * 必须排除，否则代理转发时会递归解析到自己。
 * 容器不可用或类型无对应 Bean 时降级为空集合，不影响解析链中后续来源。
 * 阶段 3 起经 {@link ObjectResolverAsSource} 桥接为注册中心贡献来源，本类作为存量实现保留。
 * 阶段 4 起为解析结果派生来源坐标：优先取 {@link IRouteMeta} 自声明的 business/version，未声明时以实现类简单名兜底
 * business（供 {@code @RoutingRef} 按业务分区分类装配本地实现）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Deprecated
public class SpringObjectResolver implements IObjectResolver {

    /**
     * 指定 Bean 名称解析
     * <p>
     * 按名称与类型精确获取容器 Bean，获取失败时降级为空集合。
     * </p>
     *
     * @param context 解析上下文，不能为null
     * @param <T>     目标实例类型
     * @return 精确匹配的解析结果集合
     */
    private <T> Collection<ResolvedObject<T>> resolveByBeanName(ObjectResolveContext<T> context) {
        try {
            T bean = SpringUtil.getBean(context.getBeanName(), context.getTargetType());
            if (RoutingServiceProxy.isRoutingProxy(bean)) {
                return Collections.emptyList();
            }
            log.debug("[SpringResolver] 按名称[{}]命中Bean, 接口: {}", context.getBeanName(),
                context.getTargetType().getName());
            return Collections.singletonList(
                ResolvedObject.of(bean, springCoordinate(bean), context.getBeanName()));
        } catch (Exception e) {
            log.debug("Spring容器按名称解析Bean失败, beanName: {}, type: {}", context.getBeanName(),
                context.getTargetType().getName());
            return Collections.emptyList();
        }
    }

    /**
     * 按类型解析容器内全部同类型 Bean
     * <p>
     * 返回的每个结果以 Bean 名称作为实例标识，容器异常时降级为空集合。
     * </p>
     *
     * @param context 解析上下文，不能为null
     * @param <T>     目标实例类型
     * @return 全部同类型 Bean 的解析结果集合
     */
    private <T> Collection<ResolvedObject<T>> resolveByType(ObjectResolveContext<T> context) {
        try {
            Map<String, T> beans = SpringUtil.getBeansOfType(context.getTargetType());
            if (beans == null || beans.isEmpty()) {
                return Collections.emptyList();
            }
            var results = beans.entrySet().stream()
                .filter(entry -> !RoutingServiceProxy.isRoutingProxy(entry.getValue()))
                .map(entry -> ResolvedObject.of(entry.getValue(), springCoordinate(entry.getValue()),
                    entry.getKey()))
                .collect(java.util.stream.Collectors.toList());
            if (log.isDebugEnabled()) {
                log.debug("[SpringResolver] 容器命中 {} 个Bean, 接口: {}, Bean: {}", results.size(),
                    context.getTargetType().getName(), results.stream().map(ResolvedObject::getInstanceKey)
                        .collect(java.util.stream.Collectors.toList()));
            }
            return results;
        } catch (Exception e) {
            log.debug("Spring容器按类型解析Bean失败, type: {}", context.getTargetType().getName(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public <T> Collection<ResolvedObject<T>> resolveAll(ObjectResolveContext<T> context) {
        if (context.hasBeanName()) {
            return resolveByBeanName(context);
        }
        return resolveByType(context);
    }

    @Override
    public <T> Optional<ResolvedObject<T>> resolveOne(ObjectResolveContext<T> context) {
        var results = resolveAll(context);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.iterator().next());
    }

    /**
     * 推导 Spring 实例的来源坐标
     * <p>
     * 优先读取 {@link IRouteMeta} 自声明的 business/version；未实现该钩子时以实现类简单名兜底 business、
     * version 留空，使同一接口下的多个本地实现天然凭 business 区分、互不碰撞。经
     * {@link ClassUtils#getUserClass(Class)} 取原始类，规避 CGLIB 代理类名后缀污染坐标。
     * </p>
     *
     * @param bean 容器实例，不能为null
     * @return Spring 来源坐标
     */
    private SourceCoordinate springCoordinate(Object bean) {
        if (bean instanceof IRouteMeta meta) {
            return SourceCoordinate.of(ObjectSource.SPRING, meta.business(), meta.version());
        }
        return SourceCoordinate.of(ObjectSource.SPRING, ClassUtils.getUserClass(bean).getSimpleName(), "");
    }

    @Override
    public ObjectSource getSource() {
        return ObjectSource.SPRING;
    }

    @Override
    public int getOrder() {
        return ProviderConstant.ORDER_SPRING_RESOLVER;
    }
}
