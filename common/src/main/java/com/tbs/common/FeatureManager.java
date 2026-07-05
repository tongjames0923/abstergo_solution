package com.tbs.common;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能管理器
 * <p>
 * 提供系统功能模块的查询能力。通过查询AbstractAutoBeanRegister中已注册的功能集合，
 * 判断某个功能是否在当前系统中得到支持。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 注入FeatureManager
 * &#64;Autowired
 * private FeatureManager featureManager;
 *
 * // 检查单个功能是否支持
 * if (featureManager.isSupported(Feature.REDIS)) {
 *     // 使用Redis功能
 * }
 *
 * // 批量检查功能是否全部支持
 * if (featureManager.isAllSupported(Feature.REDIS, Feature.RABBITMQ)) {
 *     // Redis和RabbitMQ功能都支持
 * }
 *
 * // 批量检查功能是否任一支持
 * if (featureManager.isAnySupported(Feature.REDIS, Feature.CACHE)) {
 *     // Redis或Cache功能至少有一个支持
 * }
 * </pre>
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Component
public class FeatureManager {

    /**
     * 检查指定功能是否已注册（已支持）
     *
     * @param feature 要检查的功能模块
     * @return true表示已注册（支持），false表示未注册（不支持）
     */
    public boolean isSupported(Feature feature) {
        return AbstractAutoBeanRegister.isFeatureRegistered(feature);
    }

    /**
     * 批量检查功能是否全部支持
     * <p>
     * 只有当所有指定的功能模块都已注册时才返回true
     * </p>
     *
     * @param features 要检查的功能模块数组
     * @return true表示全部支持，false表示至少有一个不支持
     */
    public boolean isAllSupported(Feature... features) {
        if (features == null || features.length == 0) {
            return true;
        }
        for (Feature feature : features) {
            if (!isSupported(feature)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 批量检查功能是否任一支持
     * <p>
     * 只要有任意一个指定的功能模块已注册就返回true
     * </p>
     *
     * @param features 要检查的功能模块数组
     * @return true表示至少有一个支持，false表示全部不支持
     */
    public boolean isAnySupported(Feature... features) {
        if (features == null || features.length == 0) {
            return false;
        }
        for (Feature feature : features) {
            if (isSupported(feature)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有已注册（支持）的功能模块集合
     *
     * @return 已注册功能模块的不可修改集合
     */
    public Set<Feature> getSupportedFeatures() {
        return AbstractAutoBeanRegister.getRegisteredFeatures();
    }

    /**
     * 获取所有已注册功能的ID集合
     *
     * @return 已注册功能ID的不可修改集合
     */
    public Set<String> getSupportedFeatureIds() {
        return Collections.unmodifiableSet(
            AbstractAutoBeanRegister.getRegisteredFeatures().stream()
                .map(Feature::getId)
                .collect(Collectors.toSet())
        );
    }

    /**
     * 检查指定功能是否未注册（不支持）
     *
     * @param feature 要检查的功能模块
     * @return true表示未注册（不支持），false表示已注册（支持）
     */
    public boolean isNotSupported(Feature feature) {
        return !isSupported(feature);
    }
}
