package com.tbs.common.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.provider.impl.DefaultRegistryCenter;
import com.tbs.common.provider.impl.DefaultRegistryEntry;
import com.tbs.common.registry.DispatchPolicy;

/**
 * 统一注册中心阶段 2 自检：驱动 {@link DefaultRegistryCenter} 的注册、按来源偏好与 business/version 过滤解析、
 * FIRST/ALL 分派、按来源增量替换与监听回调，以及代理工厂未装配时的快速失败。
 * <p>
 * 非 Spring 上下文级测试——直接坐落 {@code KeyedRegistry} 之上验证条目行为；候选来源偏好沿用当前
 * {@link RoutingStrategy}/{@link ObjectSource} 枚举语义（值对象化在阶段 3 落地，届时本测试无需改动即可通过）。
 * </p>
 *
 * @author abstergo
 */
class DefaultRegistryCenterTest {

    /**
     * LOCAL_FIRST 首选 Spring，REMOTE_FIRST 首选 Dubbo——同源命中位次决定首个条目
     */
    @Test
    void sourcePreferenceOrdersCandidates() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        center.register(entry(ObjectSource.SPRING, "", "", "springBean", 0));
        center.register(entry(ObjectSource.DUBBO, "", "", "10.0.0.1:20880", 0));

        RegistryEntry localFirst = center.resolveFirst(Target.class, null, null, RoutingStrategy.LOCAL_FIRST).orElseThrow();
        RegistryEntry remoteFirst = center.resolveFirst(Target.class, null, null, RoutingStrategy.REMOTE_FIRST).orElseThrow();

        assertEquals(ObjectSource.SPRING, localFirst.coordinate().getSource());
        assertEquals(ObjectSource.DUBBO, remoteFirst.coordinate().getSource());
    }

    /**
     * 单来源策略通过 accept 过滤掉另一来源候选
     */
    @Test
    void singleSourceStrategyFiltersTheOther() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        center.register(entry(ObjectSource.SPRING, "", "", "springBean", 0));
        center.register(entry(ObjectSource.DUBBO, "", "", "10.0.0.1:20880", 0));

        List<RegistryEntry> springOnly = center.resolve(Target.class, null, null, DispatchPolicy.ALL, RoutingStrategy.SPRING_ONLY);
        List<RegistryEntry> dubboOnly = center.resolve(Target.class, null, null, DispatchPolicy.ALL, RoutingStrategy.DUBBO_ONLY);

        assertEquals(1, springOnly.size());
        assertEquals(ObjectSource.SPRING, springOnly.get(0).coordinate().getSource());
        assertEquals(1, dubboOnly.size());
        assertEquals(ObjectSource.DUBBO, dubboOnly.get(0).coordinate().getSource());
    }

    /**
     * business/version 作为一等过滤维度收窄候选；空过滤不约束
     */
    @Test
    void businessAndVersionNarrowCandidates() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        center.register(entry(ObjectSource.DUBBO, "order", "1.0", "10.0.0.1:20880", 0));
        center.register(entry(ObjectSource.DUBBO, "inventory", "1.0", "10.0.0.1:20880", 0));

        List<RegistryEntry> orderOnly = center.resolve(Target.class, "order", null, DispatchPolicy.ALL, RoutingStrategy.DUBBO_ONLY);
        List<RegistryEntry> all = center.resolve(Target.class, null, null, DispatchPolicy.ALL, RoutingStrategy.DUBBO_ONLY);
        List<RegistryEntry> missVersion = center.resolve(Target.class, "order", "9.9", DispatchPolicy.ALL, RoutingStrategy.DUBBO_ONLY);

        assertEquals(1, orderOnly.size());
        assertEquals("order", orderOnly.get(0).coordinate().getBusiness());
        assertEquals(2, all.size());
        assertTrue(missVersion.isEmpty());
    }

    /**
     * 同来源多候选：FIRST 仅回一条，ALL 全回且按 order 降序
     */
    @Test
    void firstReturnsSingle_allReturnsOrderDesc() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        center.register(entry(ObjectSource.SPRING, "", "", "low", 1));
        center.register(entry(ObjectSource.SPRING, "", "", "high", 10));

        List<RegistryEntry> first = center.resolve(Target.class, null, null, DispatchPolicy.FIRST, RoutingStrategy.SPRING_ONLY);
        List<RegistryEntry> all = center.resolve(Target.class, null, null, DispatchPolicy.ALL, RoutingStrategy.SPRING_ONLY);

        assertEquals(1, first.size());
        assertEquals("high", first.get(0).instanceKey());
        assertEquals(2, all.size());
        assertEquals(List.of("high", "low"), all.stream().map(RegistryEntry::instanceKey).collect(Collectors.toList()));
    }

    /**
     * 按来源增量替换：目标来源旧条目摘除、新条目入桶，其他来源不受影响，并按变化坐标回调监听器
     */
    @Test
    void replaceEntriesBySourceSwapsOnlyTargetSourceAndNotifies() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        center.register(entry(ObjectSource.SPRING, "", "", "springBean", 0));
        center.register(entry(ObjectSource.DUBBO, "order", "1.0", "10.0.0.1:20880", 0));

        AtomicReference<Class<?>> changedKey = new AtomicReference<>();
        AtomicReference<SourceCoordinate> changedCoordinate = new AtomicReference<>();
        center.addListener((coordinate, key) -> {
            changedCoordinate.set(coordinate);
            changedKey.set(key);
        });

        center.replaceEntriesBySource(ObjectSource.DUBBO, Target.class,
            List.of(entry(ObjectSource.DUBBO, "order", "1.0", "10.0.0.2:20880", 0)));

        List<RegistryEntry> remainingDubbo = center.resolve(Target.class, null, null, DispatchPolicy.ALL, RoutingStrategy.DUBBO_ONLY);
        assertEquals(1, remainingDubbo.size());
        assertEquals("10.0.0.2:20880", remainingDubbo.get(0).instanceKey());
        assertFalse(center.resolve(Target.class, null, null, DispatchPolicy.ALL, RoutingStrategy.SPRING_ONLY).isEmpty());
        assertEquals(Target.class, changedKey.get());
        assertEquals(ObjectSource.DUBBO, changedCoordinate.get().getSource());
        assertEquals("order", changedCoordinate.get().getBusiness());
        assertEquals("1.0", changedCoordinate.get().getVersion());
    }

    /**
     * 代理工厂未装配时构建路由代理快速失败（阶段 3 接线前不静默返回 null）
     */
    @Test
    void getRoutingProxyFastFailsWhenFactoryAbsent() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        assertThrows(FormattedRuntimeException.class,
            () -> center.getRoutingProxy(Target.class, RoutingStrategy.LOCAL_FIRST, null, null));
    }

    /**
     * keys() 反映已登记的接口类型
     */
    @Test
    void keysReflectRegisteredInterfaces() {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        center.register(entry(ObjectSource.SPRING, "", "", "springBean", 0));
        assertTrue(center.keys().contains(Target.class));
    }

    /**
     * 构造一个指向 {@link Target} 的直连条目
     *
     * @param source     来源
     * @param business   业务分区
     * @param version    版本
     * @param instanceKey 实例物理标识
     * @param order      优先级
     * @return 注册条目
     */
    private RegistryEntry entry(ObjectSource source, String business, String version, String instanceKey, int order) {
        SourceCoordinate coordinate = SourceCoordinate.of(source, business, version);
        return DefaultRegistryEntry.ofInstance(Target.class, coordinate, instanceKey, order, new Object());
    }

    /**
     * 测试用目标接口
     */
    interface Target {
    }
}
