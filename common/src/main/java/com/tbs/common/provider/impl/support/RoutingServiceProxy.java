package com.tbs.common.provider.impl.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.provider.IObjectProvider;
import com.tbs.common.provider.ResolvedObject;
import com.tbs.common.provider.RoutingStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * 多来源路由代理调用处理器
 * <p>
 * JDK 动态代理的核心转发逻辑：每次方法调用按 {@link RoutingStrategy} 的主备来源定向解析目标实现后转发，
 * 只访问策略接受的来源对应解析器，不做全链枚举后筛选。
 * 两来源均未命中时按统一失败语义返回 null 并记录 warn 日志；但方法返回值为基本类型时
 * 返回 null 会在调用方拆箱时抛出难以定位的 NullPointerException，故显式抛出带 i18n 文案的异常。
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
     * 统一对象提供器
     */
    private final IObjectProvider objectProvider;

    /**
     * 构造路由代理调用处理器
     *
     * @param interfaceType  目标接口类型，不能为null
     * @param strategy       路由策略，不能为null
     * @param objectProvider 统一对象提供器，不能为null
     */
    public RoutingServiceProxy(Class<T> interfaceType, RoutingStrategy strategy, IObjectProvider objectProvider) {
        this.interfaceType = interfaceType;
        this.strategy = strategy;
        this.objectProvider = objectProvider;
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
     * 按策略解析目标实例
     * <p>
     * 先取策略首选来源，未命中时降级到备选来源；单来源策略（仅本地/仅远程）不会跨源降级。
     * </p>
     *
     * @return 目标实例，策略接受的来源均未命中时为 empty
     */
    private Optional<ResolvedObject<T>> resolveTarget() {
        var primary = strategy.primarySource();
        if (!strategy.accept(primary)) {
            return objectProvider.getResolvedObject(interfaceType, strategy.backupSource());
        }
        var hit = objectProvider.getResolvedObject(interfaceType, primary);
        if (hit.isPresent() || !strategy.accept(strategy.backupSource())) {
            return hit;
        }
        return objectProvider.getResolvedObject(interfaceType, strategy.backupSource());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return handleObjectMethod(proxy, method, args);
        }
        var target = resolveTarget();
        if (!target.isPresent()) {
            log.warn("路由代理未命中任何来源实例, 接口: {}, 策略: {}, 方法: {}", interfaceType.getName(), strategy,
                method.getName());
            AssertUtil.isTrue(method.getReturnType().isPrimitive())
                .i18n(CommonI18nConstant.ROUTING_TARGET_NOT_FOUND, interfaceType.getName(), strategy.name(),
                    method.getName())
                .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
            return null;
        }
        try {
            return method.invoke(target.get().getInstance(), args);
        } catch (InvocationTargetException e) {
            throw e.getCause() == null ? e : e.getCause();
        }
    }
}
