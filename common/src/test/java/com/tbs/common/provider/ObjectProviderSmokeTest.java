package com.tbs.common.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;

import org.apache.dubbo.common.URL;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tbs.common.config.ObjectProviderConfig;
import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.provider.annotation.RoutingService;
import com.tbs.common.provider.impl.SpringObjectResolver;
import com.tbs.common.provider.impl.support.InterfaceProviderState;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一对象提供器冒烟测试
 * <p>
 * 验证核心场景：Spring 来源解析命中、getObjects 多来源枚举、LOCAL_FIRST 路由代理本地直取、
 * @RoutingService 注解扫描注册代理 Bean 注入即用（按名称与按类型均可用）、无本地实现时代理降级返回null、
 * 路由代理作为「通道」不参与实现枚举、实例标识匹配语义、整体引用判定、Dubbo 实例快照的空快照可重查语义。
 * 测试环境无 Dubbo 注册中心，Dubbo 来源解析降级为空集合（warn 日志为预期行为）。
 * 测试接口与实现为顶级类型，与生产中业务接口的组织方式一致。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
class ObjectProviderSmokeTest {

    /**
     * 路由代理 Bean 名称：接口名首字母小写 + 固定后缀
     */
    private static final String ROUTED_PROXY_BEAN_NAME = "routedServiceRoutingProxy";

    /**
     * 构建 Spring 测试容器
     *
     * @return 注解配置应用上下文
     */
    private AnnotationConfigApplicationContext buildContext() {
        var context = new AnnotationConfigApplicationContext();
        context.register(SpringObjectResolver.class, ObjectProviderConfig.class);
        return context;
    }

    /**
     * 验证 Spring 来源解析、Optional 语义、多来源枚举与路由代理本地直取
     */
    @Test
    void testSpringResolutionAndRouting() {
        try (var context = buildContext()) {
            context.registerBean(DefaultEchoService.class);
            context.refresh();

            var provider = context.getBean(IObjectProvider.class);

            assertEquals("echo:hi", provider.getObject(EchoService.class).echo("hi"));
            assertTrue(provider.getObjectOptional(EchoService.class).isPresent());
            assertNull(provider.getObject(RoutedService.class));

            Collection<ResolvedObject<EchoService>> objects = provider.getObjects(EchoService.class);
            assertEquals(1, objects.size());
            var resolved = objects.iterator().next();
            assertEquals(ObjectSource.SPRING, resolved.getSource());

            EchoService proxy = provider.getRoutingProxy(EchoService.class, RoutingStrategy.LOCAL_FIRST);
            assertEquals("echo:proxy", proxy.echo("proxy"));
        }
    }

    /**
     * 验证 @RoutingService 注解扫描注册的代理 Bean 注入即用与本地路由
     * <p>
     * 容器内同类型存在「本地实现 + 路由代理」两个 Bean，按类型注入必须命中代理（依赖 primary 标记），
     * 否则业务侧按类型注入会因候选不唯一而失败。
     * </p>
     */
    @Test
    void testRoutingServiceRegistrarProxyInjection() {
        try (var context = buildContext()) {
            context.registerBean(DefaultRoutedService.class);
            context.refresh();

            var routed = context.getBean(ROUTED_PROXY_BEAN_NAME, RoutedService.class);
            assertEquals("pong", routed.ping());
            assertEquals("pong", context.getBean(RoutedService.class).ping());

            var provider = context.getBean(IObjectProvider.class);
            assertTrue(provider.getObjectOptional(RoutedService.class).isPresent());

            // 代理不参与实现枚举：容器内两个同类型 Bean 只应解析出本地实现一个
            Collection<ResolvedObject<RoutedService>> objects = provider.getObjects(RoutedService.class);
            assertEquals(1, objects.size());
            assertEquals("defaultRoutedService", objects.iterator().next().getInstanceKey());
        }
    }

    /**
     * 验证无本地实现且无 Dubbo 来源时，路由代理按失败语义返回null
     */
    @Test
    void testRoutingProxyFallbackToNull() {
        try (var context = buildContext()) {
            context.refresh();

            var provider = context.getBean(IObjectProvider.class);
            var routed = context.getBean(ROUTED_PROXY_BEAN_NAME, RoutedService.class);
            assertNull(routed.ping());
            assertTrue(provider.getObjects(RoutedService.class).isEmpty());
        }
    }

    /**
     * 验证实例标识匹配语义：指定标识的解析请求不得被降级为其他实例
     */
    @Test
    void testInstanceKeyMatching() {
        assertTrue(ObjectResolveContext.of(EchoService.class).matchesInstanceKey("any"));
        assertTrue(ObjectResolveContext.of(EchoService.class, "localBean").matchesInstanceKey("localBean"));
        assertFalse(ObjectResolveContext.of(EchoService.class, "localBean").matchesInstanceKey("127.0.0.1:20880"));
    }

    /**
     * 验证整体引用判定：仅 Dubbo 来源且实例标识为整体引用标识时成立，避免调用方比对魔法值
     */
    @Test
    void testClusterReferenceDetection() {
        var clusterKey = ProviderConstant.DUBBO_CLUSTER_INSTANCE_KEY;
        assertTrue(ResolvedObject.of(new DefaultEchoService(), ObjectSource.DUBBO, clusterKey).isClusterReference());
        assertFalse(ResolvedObject.of(new DefaultEchoService(), ObjectSource.DUBBO, "127.0.0.1:20880")
            .isClusterReference());
        assertFalse(ResolvedObject.of(new DefaultEchoService(), ObjectSource.SPRING, clusterKey)
            .isClusterReference());
    }

    /**
     * 验证 Dubbo 实例快照的新鲜度判定
     * <p>
     * 空快照必须始终可重查，否则首次发现早于服务上线时该来源会被永久锁死；
     * 非空快照在刷新间隔内不过期，且过期判定前不会被后续刷新覆盖。
     * </p>
     */
    @Test
    void testProviderSnapshotFreshness() {
        var providerUrl = URL.valueOf("dubbo://127.0.0.1:20880/com.tbs.common.provider.EchoService");
        var state = new InterfaceProviderState<>(EchoService.class);
        assertTrue(state.isStale(5000L), "尚未尝试过时空快照应触发首次发现");
        assertTrue(state.isStale(-1L), "空快照重查不受配置刷新间隔限制");

        state.refresh(List.of(providerUrl), 5000L);
        assertEquals(1, state.getProviderUrls().size());
        assertFalse(state.isStale(5000L), "非空快照在刷新间隔内不应过期");

        state.refresh(List.of(), 5000L);
        assertEquals(1, state.getProviderUrls().size(), "未过期时不应被新的发现结果覆盖");

        state.refresh(List.of(), -1L);
        assertEquals(1, state.getProviderUrls().size(), "刷新间隔为非正数时快照永不主动过期");
        state.destroy();
    }
}
