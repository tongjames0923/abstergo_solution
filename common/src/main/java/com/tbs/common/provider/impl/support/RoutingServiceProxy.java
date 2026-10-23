package com.tbs.common.provider.impl.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import com.tbs.common.provider.INoProviderHandler;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.IRemoteTargetResolver;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.provider.RoutingStrategy;
import com.tbs.common.provider.annotation.RoutingService;
import com.tbs.common.provider.impl.LoggingNoProviderHandler;
import com.tbs.common.util.framework.SpringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 多来源路由代理调用处理器
 * <p>
 * JDK 动态代理的核心转发逻辑：每次方法调用按 {@link RoutingStrategy#preference()} 的来源偏好逐级择一，
 * 命中目标后转发。来源分两类通道：注册中心内以条目枚举的来源（如 Spring 本地 Bean）经
 * {@link IRegistryCenter#resolveFirst} 按绑定 business/version 维度过滤命中；不在中心枚举、交由框架自身发现
 * 的远程来源（如 Dubbo）经 {@link IRemoteTargetResolver} 直接索取绑定维度的远程目标。远程目标的存在性由
 * 框架在调用期保障（无提供者时调用抛框架异常，而非解析期判空）。
 * 全部偏好来源均未命中时记录框架级 warn 日志后，交由接口 {@link RoutingService#noProvider()} 声明的
 * {@link INoProviderHandler} 处理（默认仅记日志，可切换为抛异常等策略）；处理器返回后代理一律返回 null。
 * </p>
 *
 * @param <T> 目标接口类型
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class RoutingServiceProxy<T> implements InvocationHandler {

    /**
     * 目标接口类型
     */
    private final Class<T> interfaceType;

    /**
     * 路由策略
     */
    private final RoutingStrategy strategy;

    /**
     * 绑定业务分区，null/空串表示不约束
     */
    private final String business;

    /**
     * 绑定发布/灰度版本，null/空串表示不约束
     */
    private final String version;

    /**
     * 统一注册中心（阶段 3-δ 起路由解析的唯一入口）
     */
    private final IRegistryCenter registryCenter;

    /**
     * 无提供者处理器：取自接口 {@link RoutingService#noProvider()}，未标注时回退默认记日志处理器
     */
    private final INoProviderHandler noProviderHandler;

    /**
     * 远程来源目标解析器，按来源索引；空表示无远程来源参与路由
     */
    private final Map<ObjectSource, IRemoteTargetResolver> remoteResolvers;

    /**
     * 构造路由代理调用处理器
     *
     * @param interfaceType  目标接口类型，不能为null
     * @param registryCenter 统一注册中心，不能为null
     * @param strategy       路由策略，不能为null
     * @param business       绑定业务分区，可为null表示不约束
     * @param version        绑定发布版本，可为null表示不约束
     * @param remoteResolvers 远程来源目标解析器集合，按来源索引，可为空
     */
    public RoutingServiceProxy(Class<T> interfaceType, IRegistryCenter registryCenter, RoutingStrategy strategy,
        String business, String version, Map<ObjectSource, IRemoteTargetResolver> remoteResolvers) {
        this.interfaceType = interfaceType;
        this.registryCenter = registryCenter;
        this.strategy = strategy;
        this.business = business;
        this.version = version;
        this.remoteResolvers = remoteResolvers == null ? Map.of() : remoteResolvers;
        this.noProviderHandler = resolveNoProviderHandler(interfaceType);
    }

    /**
     * 解析接口的无提供者处理器实例
     * <p>
     * 读取 {@link RoutingService#noProvider()} 声明的处理类（接口未标注注解时回退默认记日志处理器），
     * 优先从 Spring 容器按类型获取（内置处理器均为 {@code @Component}），取不到再反射实例化兜底，
     * 反射失败最终回退默认处理器，保证代理构建不因处理器解析失败而中断。
     * </p>
     *
     * @param interfaceType 目标接口类型，不能为null
     * @return 无提供者处理器实例，不会为null
     */
    private static INoProviderHandler resolveNoProviderHandler(Class<?> interfaceType) {
        RoutingService routing = interfaceType.getAnnotation(RoutingService.class);
        Class<? extends INoProviderHandler> handlerClass =
            routing != null ? routing.noProvider() : LoggingNoProviderHandler.class;
        var bean = SpringUtil.getBeanOptional(handlerClass);
        if (bean.isPresent()) {
            return bean.get();
        }
        try {
            return handlerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.warn("无提供者处理器实例化失败, 回退默认记日志处理器, 处理类: {}, 原因: {}",
                handlerClass.getName(), e.getMessage());
            return new LoggingNoProviderHandler();
        }
    }

    /**
     * 处理 Object 基础方法
     * <p>
     * hashCode/equals/toString 直传代理自身语义，toString 中附带接口与策略信息便于调试定位。
     * </p>
     *
     * @param proxy 代理实例
     * @param method 被调用的方法
     * @param args  方法参数
     * @return 基础方法执行结果，非基础方法返回null
     */
    private Object handleObjectMethod(Object proxy, Method method, Object[] args) {
        var methodName = method.getName();
        if ("hashCode".equals(methodName)) {
            return System.identityHashCode(proxy);
        }
        if ("equals".equals(methodName)) {
            return proxy == args[0];
        }
        if ("toString".equals(methodName)) {
            return "RoutingServiceProxy{interface=" + interfaceType.getName() + ", strategy=" + strategy + "}";
        }
        return null;
    }

    /**
     * 判断候选对象是否为路由代理
     * <p>
     * 路由代理以接口类型注册进容器后，会被来源解析枚举为该接口的「实现」，
     * 但代理是转发通道而非实现，各来源解析器必须据此排除，否则代理转发时再次解析到自己造成无限递归。
     * 按调用处理器类型判定而非按实例集合判定，因此不局限于某个提供器实例创建的代理。
     * </p>
     *
     * @param candidate 待判定的候选对象，允许为null
     * @return true 表示该对象为路由代理
     */
    public static boolean isRoutingProxy(Object candidate) {
        return candidate != null && Proxy.isProxyClass(candidate.getClass())
            && Proxy.getInvocationHandler(candidate) instanceof RoutingServiceProxy;
    }

    /**
     * 按策略偏好逐级择一路由目标
     * <p>
     * 遵 {@link RoutingStrategy#preference()} 的优先序逐个来源尝试：命中 {@link #remoteResolvers} 登记的远程来源
     * 那么向远程解析器索取目标（返回 null 表示来源不可用，降级下一优先级）；否则经中心按单来源策略与绑定维度
     * 取首个枚举条目。business/version 为代理创建时绑定的定向维度，null/空表示不约束。
     * </p>
     *
     * @return 命中的路由目标实例，全部偏好来源均未命中时为 null
     */
    private Object selectTarget() {
        for (ObjectSource source : strategy.preference()) {
            var resolver = remoteResolvers.get(source);
            if (resolver != null) {
                var remote = resolver.resolveTarget(interfaceType, business, version);
                if (remote != null) {
                    return remote;
                }
                continue;
            }
            var hit = registryCenter.resolveFirst(interfaceType, business, version, RoutingStrategy.of(source));
            if (hit.isPresent()) {
                return hit.get().resolve();
            }
        }
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return handleObjectMethod(proxy, method, args);
        }
        var target = selectTarget();
        if (target == null) {
            log.warn("路由代理未命中任何来源实例, 接口: {}, 策略: {}, 方法: {}", interfaceType.getName(), strategy,
                method.getName());
            noProviderHandler.handle(interfaceType, method.getName(), args);
            return null;
        }
        try {
            log.debug("[RoutingProxy] 路由转发, 接口: {}, 方法: {}, 策略: {}, 目标: {}", interfaceType.getName(),
                method.getName(), strategy, target.getClass().getName());
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause() == null ? e : e.getCause();
        }
    }
}
