package com.tbs.common.config.property;

import com.tbs.common.constant.ProviderConstant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 对象提供器配置属性类
 * <p>
 * 通过@ConfigurationProperties绑定tbs.common.object-provider前缀的配置属性，
 * 集中管理多来源对象提供器的功能开关与 Dubbo 实例发现参数，替代零散的@Value注入。
 * 不使用@Component注册，改由ObjectProviderConfig经@EnableConfigurationProperties显式激活，
 * 避免重复导入时重复注册。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(ProviderConstant.PROPERTY_PREFIX)
public class ObjectProviderProperty {

    /**
     * 功能总开关，默认开启（框架基础能力，跟随EnableCommonPack生效）
     */
    private boolean enable = true;

    /**
     * Dubbo实例发现开关，关闭后解析链仅剩Spring来源，路由策略自动退化为本地直取
     */
    private boolean dubboDiscoveryEnabled = true;

    /**
     * Dubbo引用超时时间（毫秒），作用于整体引用与实例直连引用
     */
    private int dubboTimeout = ProviderConstant.DEFAULT_DUBBO_TIMEOUT;

    /**
     * Dubbo实例快照刷新间隔（毫秒），非正数表示发现到实例后不再主动过期；
     * 未发现到实例时的空快照按固定回退间隔（不受本配置影响）重新发现
     */
    private long dubboDiscoveryRefreshMillis = ProviderConstant.DEFAULT_DISCOVERY_REFRESH_MILLIS;
}
