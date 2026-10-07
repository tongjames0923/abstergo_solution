package com.tbs.common.constant;

/**
 * 对象提供器模块常量
 * <p>
 * 收拢 com.tbs.common.provider 包使用的常量：配置前缀、Dubbo 引用默认值、缓存键格式等，禁止散落魔法值。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class ProviderConstant {

    /**
     * 对象提供器配置前缀
     */
    public static final String PROPERTY_PREFIX = "tbs.common.object-provider";

    /**
     * 对象提供器功能总开关属性名
     */
    public static final String ENABLE_PROPERTY_NAME = "enable";

    /**
     * Dubbo 引用默认超时时间（毫秒）
     */
    public static final int DEFAULT_DUBBO_TIMEOUT = 10000;

    /**
     * Dubbo 服务提供者地址分隔格式：host:port
     */
    public static final String DUBBO_INSTANCE_KEY_FORMAT = "%s:%d";

    /**
     * Dubbo 整体负载均衡引用的实例标识，用于区别于逐实例直连引用的 host:port
     */
    public static final String DUBBO_CLUSTER_INSTANCE_KEY = "cluster";

    /**
     * 路由代理默认 Bean 名称后缀，避免与业务侧同名的本地实现 Bean 冲突
     */
    public static final String ROUTING_PROXY_BEAN_NAME_SUFFIX = "RoutingProxy";

    /**
     * Dubbo 实例快照默认刷新间隔（毫秒）
     */
    public static final long DEFAULT_DISCOVERY_REFRESH_MILLIS = 5000L;

    /**
     * Dubbo 空快照（未发现到任何实例）的固定重新发现回退间隔（毫秒）
     */
    public static final long EMPTY_DISCOVERY_RETRY_MILLIS = 1000L;

    /**
     * 解析器优先级：Spring 来源解析器，最高优先级
     */
    public static final int ORDER_SPRING_RESOLVER = -1000;

    /**
     * 解析器优先级：Dubbo 实例发现解析器
     */
    public static final int ORDER_DUBBO_RESOLVER = 1000;

    private ProviderConstant() {
    }
}
