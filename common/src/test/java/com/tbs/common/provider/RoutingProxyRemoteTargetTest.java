package com.tbs.common.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.tbs.common.provider.impl.DefaultRegistryCenter;
import com.tbs.common.provider.impl.ObjectResolverAsSource;
import com.tbs.common.provider.impl.support.RoutingServiceProxy;

/**
 * 路由代理跨「枚举来源」与「远程来源」择一自检（3-ε Dubbo 原生发现改造）。
 * <p>
 * 以桩 Spring 解析器（枚举通道，经中心）与桩 {@link IRemoteTargetResolver}（远程通道）驱动
 * {@link RoutingServiceProxy}，验证：{@code LOCAL_FIRST} 本地优先、本地缺失降级远程、{@code REMOTE_FIRST}
 * 远程优先、{@code SPRING_ONLY} 从不触达远程、远程不可用（返回 null）且本地缺失时走 noProvider 返回 null、
 * business/version 原样透传给远程解析器。核心逻辑 {@code selectTarget} 按 {@code strategy.preference()} 逐级择一。
 * </p>
 *
 * @author abstergo
 */
@SuppressWarnings("deprecation")
class RoutingProxyRemoteTargetTest {

    /**
     * 被路由的目标接口：ping 返回来源标记，便于断言最终命中哪条通道
     */
    private interface Sample {

        /**
         * 返回命中的来源标识
         *
         * @return 来源标记
         */
        String ping();
    }

    /**
     * 可开关的桩 Spring 解析器：{@code hasBean} 为真时产出一个返回 "SPRING" 的可路由条目
     */
    private static class SwitchableSpringResolver implements IObjectResolver {

        private final boolean hasBean;

        SwitchableSpringResolver(boolean hasBean) {
            this.hasBean = hasBean;
        }

        @Override
        public ObjectSource getSource() {
            return ObjectSource.SPRING;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Collection<ResolvedObject<T>> resolveAll(ObjectResolveContext<T> context) {
            if (!hasBean) {
                return new ArrayList<>();
            }
            List<ResolvedObject<T>> result = new ArrayList<>();
            result.add((ResolvedObject<T>) ResolvedObject.of((Sample) () -> "SPRING",
                SourceCoordinate.ofSource(ObjectSource.SPRING), "springBean"));
            return result;
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    /**
     * 桩远程解析器：可选「是否可用」，返回 "DUBBO" 目标并记录收到的 business/version
     */
    private static class StubRemoteResolver implements IRemoteTargetResolver {

        private final boolean available;

        private String lastBusiness;

        private String lastVersion;

        StubRemoteResolver(boolean available) {
            this.available = available;
        }

        @Override
        public ObjectSource source() {
            return ObjectSource.DUBBO;
        }

        @Override
        public <T> T resolveTarget(Class<T> interfaceType, String business, String version) {
            this.lastBusiness = business;
            this.lastVersion = version;
            if (!available) {
                return null;
            }
            return interfaceType.cast((Sample) () -> "DUBBO");
        }
    }

    /**
     * 构建绑定 business/version 的路由代理
     *
     * @param center          注册中心
     * @param strategy        路由策略
     * @param business        业务分区，可为null
     * @param version         发布版本，可为null
     * @param remoteResolvers 远程解析器映射
     * @return Sample 代理实例
     */
    private static Sample buildProxy(IRegistryCenter center, RoutingStrategy strategy, String business,
        String version, Map<ObjectSource, IRemoteTargetResolver> remoteResolvers) {
        return (Sample) Proxy.newProxyInstance(Sample.class.getClassLoader(), new Class<?>[] {Sample.class},
            new RoutingServiceProxy<>(Sample.class, center, strategy, business, version, remoteResolvers));
    }

    /**
     * LOCAL_FIRST：本地 Bean 存在时优先命中 Spring，不触达远程
     */
    @Test
    void localFirstPrefersSpringWhenPresent() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        new ObjectResolverAsSource(new SwitchableSpringResolver(true), false).contribute(center);
        StubRemoteResolver remote = new StubRemoteResolver(true);
        Sample proxy = buildProxy(center, RoutingStrategy.LOCAL_FIRST, null, null,
            Map.of(ObjectSource.DUBBO, remote));

        assertEquals("SPRING", proxy.ping());
    }

    /**
     * LOCAL_FIRST：本地缺失时降级命中远程，且 business/version 原样透传
     */
    @Test
    void localFirstFallsBackToRemoteAndPassesCoordinate() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        new ObjectResolverAsSource(new SwitchableSpringResolver(false), false).contribute(center);
        StubRemoteResolver remote = new StubRemoteResolver(true);
        Sample proxy = buildProxy(center, RoutingStrategy.LOCAL_FIRST, "order", "1.0",
            Map.of(ObjectSource.DUBBO, remote));

        assertEquals("DUBBO", proxy.ping());
        assertEquals("order", remote.lastBusiness);
        assertEquals("1.0", remote.lastVersion);
    }

    /**
     * REMOTE_FIRST：远程可用时优先命中远程，即便本地 Bean 也存在
     */
    @Test
    void remoteFirstPrefersRemote() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        new ObjectResolverAsSource(new SwitchableSpringResolver(true), false).contribute(center);
        StubRemoteResolver remote = new StubRemoteResolver(true);
        Sample proxy = buildProxy(center, RoutingStrategy.REMOTE_FIRST, null, null,
            Map.of(ObjectSource.DUBBO, remote));

        assertEquals("DUBBO", proxy.ping());
    }

    /**
     * SPRING_ONLY：从不触达远程解析器；本地缺失时走 noProvider 返回 null
     */
    @Test
    void springOnlyNeverTouchesRemote() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        new ObjectResolverAsSource(new SwitchableSpringResolver(false), false).contribute(center);
        StubRemoteResolver remote = new StubRemoteResolver(true);
        Sample proxy = buildProxy(center, RoutingStrategy.SPRING_ONLY, null, null,
            Map.of(ObjectSource.DUBBO, remote));

        assertNull(proxy.ping());
        assertNull(remote.lastBusiness);
    }

    /**
     * LOCAL_FIRST：远程不可用（返回 null）且本地缺失时，全部偏好落空，走 noProvider 返回 null
     */
    @Test
    void remoteUnavailableAndNoLocalReturnsNull() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        new ObjectResolverAsSource(new SwitchableSpringResolver(false), false).contribute(center);
        StubRemoteResolver remote = new StubRemoteResolver(false);
        Sample proxy = buildProxy(center, RoutingStrategy.LOCAL_FIRST, null, null,
            Map.of(ObjectSource.DUBBO, remote));

        assertNull(proxy.ping());
    }
}
