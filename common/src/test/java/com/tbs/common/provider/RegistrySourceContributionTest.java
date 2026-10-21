package com.tbs.common.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.provider.impl.DefaultRegistryCenter;
import com.tbs.common.provider.impl.ObjectResolverAsSource;
import com.tbs.common.registry.DispatchPolicy;

/**
 * 阶段 3-γ 来源贡献中心自检：以桩解析器驱动 {@link ObjectResolverAsSource} 桥接与
 * {@link DefaultRegistryCenter} 按需懒填充，验证「装配不预扫、首读贡献、读即重贡献、
 * 同集幂等不产生监听噪音、坐标拆分使同源多业务条目并存」的核心链路语义。
 *
 * @author abstergo
 */
class RegistrySourceContributionTest {

    /**
     * 桩解析器：按预设坐标序列产出解析结果，并记录被调用次数
     */
    @SuppressWarnings("deprecation")
    private static class StubResolver implements IObjectResolver {

        private final ObjectSource source;

        private final List<SourceCoordinate> coordinates;

        private final AtomicInteger resolveCount = new AtomicInteger();

        StubResolver(ObjectSource source, List<SourceCoordinate> coordinates) {
            this.source = source;
            this.coordinates = coordinates;
        }

        @Override
        public <T> Collection<ResolvedObject<T>> resolveAll(ObjectResolveContext<T> context) {
            resolveCount.incrementAndGet();
            List<ResolvedObject<T>> result = new ArrayList<>();
            for (SourceCoordinate coordinate : coordinates) {
                @SuppressWarnings("unchecked")
                T instance = (T) new Object();
                result.add(ResolvedObject.of(instance, coordinate,
                    coordinate.getSource().id() + ':' + coordinate.getBusiness()));
            }
            return result;
        }

        @Override
        public ObjectSource getSource() {
            return source;
        }

        /**
         * 桩：实例自身即路由目标（等价于 Spring 来源），不重复计咨询次数，保持 resolveCount 语义为「解析链被读取次数」
         */
        @Override
        public <T> Collection<ResolvedObject<T>> resolveRoutableTargets(ObjectResolveContext<T> context) {
            return resolveAllWithoutCount(context);
        }

        /**
         * 按预设坐标产出解析结果，不递增咨询计数
         *
         * @param context 解析上下文，不能为null
         * @param <T>     目标类型
         * @return 解析结果集合
         */
        @SuppressWarnings("unchecked")
        private <T> List<ResolvedObject<T>> resolveAllWithoutCount(ObjectResolveContext<T> context) {
            List<ResolvedObject<T>> result = new ArrayList<>();
            for (SourceCoordinate coordinate : coordinates) {
                T instance = (T) new Object();
                result.add(ResolvedObject.of(instance, coordinate,
                    coordinate.getSource().id() + ':' + coordinate.getBusiness()));
            }
            return result;
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    /**
     * 装配贡献只登记贡献者不解析，接口首次读取才触发贡献；一次性贡献者后续读取不重扫
     */
    @Test
    @SuppressWarnings("deprecation")
    void staticSourceContributesOnFirstReadOnly() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        StubResolver resolver = new StubResolver(ObjectSource.SPRING,
            List.of(SourceCoordinate.ofSource(ObjectSource.SPRING)));
        new ObjectResolverAsSource(resolver, false).contribute(center);

        assertEquals(0, resolver.resolveCount.get());
        center.resolve(Target.class, null, null, DispatchPolicy.ALL, RoutingStrategy.SPRING_ONLY);
        center.resolve(Target.class, null, null, DispatchPolicy.ALL, RoutingStrategy.SPRING_ONLY);

        assertEquals(1, resolver.resolveCount.get());
        assertEquals(1, center.resolve(Target.class, null, null, DispatchPolicy.ALL,
            RoutingStrategy.SPRING_ONLY).size());
    }

    /**
     * 读即重贡献来源每次读取都回调，但同集幂等不产生第二次换位监听噪音
     */
    @Test
    @SuppressWarnings("deprecation")
    void refreshOnReadContributorStaysQuietWhenSnapshotUnchanged() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        StubResolver resolver = new StubResolver(ObjectSource.DUBBO,
            List.of(SourceCoordinate.of(ObjectSource.DUBBO, "order", "1.0")));
        new ObjectResolverAsSource(resolver, true).contribute(center);

        AtomicInteger changed = new AtomicInteger();
        center.addListener((coordinate, key) -> changed.incrementAndGet());

        center.resolve(Target.class, null, null, DispatchPolicy.ALL, RoutingStrategy.DUBBO_ONLY);
        center.resolve(Target.class, null, null, DispatchPolicy.ALL, RoutingStrategy.DUBBO_ONLY);

        assertEquals(2, resolver.resolveCount.get());
        assertEquals(1, changed.get());
    }

    /**
     * 同一来源同机不同 business/version 的 provider 经坐标拆分后在中心并存，且可按维度定向命中
     */
    @Test
    @SuppressWarnings("deprecation")
    void sameHostDifferentCoordinateEntriesCoexist() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        StubResolver resolver = new StubResolver(ObjectSource.DUBBO, List.of(
            SourceCoordinate.of(ObjectSource.DUBBO, "order", "1.0"),
            SourceCoordinate.of(ObjectSource.DUBBO, "inventory", "1.0")));
        new ObjectResolverAsSource(resolver, true).contribute(center);

        List<RegistryEntry> all = center.resolve(Target.class, null, null, DispatchPolicy.ALL,
            RoutingStrategy.DUBBO_ONLY);
        List<RegistryEntry> orderOnly = center.resolve(Target.class, "order", null, DispatchPolicy.ALL,
            RoutingStrategy.DUBBO_ONLY);

        assertEquals(2, all.size());
        assertEquals(1, orderOnly.size());
        assertEquals("order", orderOnly.get(0).coordinate().getBusiness());
        assertTrue(center.resolve(Target.class, "missing", null, DispatchPolicy.ALL,
            RoutingStrategy.DUBBO_ONLY).isEmpty());
    }

    /**
     * 3-δ 核心：择一路由命中 routable 集群条目、枚举视图命中 enumerable 逐实例条目，两视图分离
     * <p>
     * Dubbo 来源的逐实例直连引用仅用于枚举，集群整体引用仅作为路由目标；路由命中集群引用才能保留 Dubbo 负载均衡/failover。
     * </p>
     */
    @Test
    @SuppressWarnings("deprecation")
    void routingHitsClusterEntryWhileEnumerationKeepsInstances() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        new ObjectResolverAsSource(new DubboLikeStubResolver(), true).contribute(center);

        List<RegistryEntry> enumerated = center.resolve(Target.class, "default", null, DispatchPolicy.ALL,
            RoutingStrategy.DUBBO_ONLY);
        Optional<RegistryEntry> routed = center.resolveFirst(Target.class, null, null, RoutingStrategy.DUBBO_ONLY);

        assertEquals(2, enumerated.size());
        assertTrue(enumerated.stream().noneMatch(e -> ProviderConstant.DUBBO_CLUSTER_INSTANCE_KEY.equals(e.instanceKey())));
        assertTrue(enumerated.stream().allMatch(e -> e.enumerable() && !e.routable()));
        assertTrue(routed.isPresent());
        assertEquals(ProviderConstant.DUBBO_CLUSTER_INSTANCE_KEY, routed.get().instanceKey());
        assertTrue(routed.get().routable() && !routed.get().enumerable());
    }

    /**
     * Dubbo 风格桩：resolveAll 产出逐实例直连引用，resolveRoutableTargets 产出按坐标限定的单一集群引用
     */
    @SuppressWarnings("deprecation")
    private static class DubboLikeStubResolver implements IObjectResolver {

        private final SourceCoordinate coordinate = SourceCoordinate.of(ObjectSource.DUBBO, "default",
            "");

        @Override
        public <T> Collection<ResolvedObject<T>> resolveAll(ObjectResolveContext<T> context) {
            List<ResolvedObject<T>> result = new ArrayList<>();
            for (String host : new String[] {"10.0.0.1:20880", "10.0.0.2:20880"}) {
                result.add(castTo(ResolvedObject.of(new Object(), coordinate, host)));
            }
            return result;
        }

        @Override
        public <T> Collection<ResolvedObject<T>> resolveRoutableTargets(ObjectResolveContext<T> context) {
            return List.of(castTo(ResolvedObject.of(new Object(), coordinate,
                ProviderConstant.DUBBO_CLUSTER_INSTANCE_KEY)));
        }

        /**
         * 将持有 Object 实例的解析结果转型为目标类型泛型（桩专用，避免泛型擦除下的强转告警）
         *
         * @param resolved 解析结果，不能为null
         * @param <T>      目标类型
         * @return 转型后的解析结果
         */
        @SuppressWarnings("unchecked")
        private <T> ResolvedObject<T> castTo(ResolvedObject<?> resolved) {
            return (ResolvedObject<T>) resolved;
        }

        @Override
        public ObjectSource getSource() {
            return ObjectSource.DUBBO;
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    /**
     * 测试用目标接口
     */
    interface Target {
    }
}
