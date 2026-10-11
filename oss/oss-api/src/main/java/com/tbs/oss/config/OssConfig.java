package com.tbs.oss.config;

import com.tbs.oss.config.property.OssProperty;
import com.tbs.oss.core.OssClient;
import com.tbs.oss.core.OssClientFactory;
import com.tbs.oss.exception.OssException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

/**
 * OSS核心配置类
 * <p>
 * 根据tbs.oss配置装配统一门面{@link OssClient}：
 * 从容器中收集所有{@link OssClientFactory}实现（由各第三方实现模块提供），
 * 按tbs.oss.type匹配对应工厂创建客户端
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(OssProperty.class)
public class OssConfig {

    @Resource
    OssProperty property;

    /**
     * 创建OSS统一门面客户端实例
     * <p>
     * 仅当tbs.oss.enable属性为true且容器中不存在OssClient实例时创建，
     * 应用关闭时自动调用close释放底层SDK资源。
     * 此开关刻意默认关闭（matchIfMissing缺省为false）：OSS依赖外部MinIO/OSS服务，
     * 未配置endpoint等参数时启用会创建失败，需显式开启。
     * </p>
     *
     * @param factoryProvider 容器中所有OssClientFactory实现的提供者
     * @return OSS统一门面客户端实例
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(OssClient.class)
    @ConditionalOnProperty(prefix = "tbs.oss", value = "enable", havingValue = "true")
    public OssClient ossClient(ObjectProvider<OssClientFactory> factoryProvider) {
        String type = property.getType();
        if (type == null || type.isEmpty()) {
            throw new OssException("tbs.oss.type未配置，无法确定OSS实现类型");
        }
        OssClientFactory factory = factoryProvider.orderedStream()
            .filter(f -> type.equalsIgnoreCase(f.supportType()))
            .findFirst()
            .orElseThrow(() -> new OssException(
                "未找到类型[" + type + "]对应的OssClientFactory实现，请确认已引入对应的OSS实现模块"));
        return factory.create(property.toOssProperties());
    }
}
