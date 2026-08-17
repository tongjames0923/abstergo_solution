package com.tbs.oss.config.property;

import com.tbs.oss.model.OssProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OSS配置属性类
 * <p>
 * 通过@ConfigurationProperties注解绑定tbs.oss前缀的配置属性
 * 用于配置OSS功能的开关及连接信息，具体实现类型由tbs.oss.type指定，
 * 对应的实现模块需提供相应的OssClientFactory
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@ConfigurationProperties("tbs.oss")
public class OssProperty {

    /**
     * 是否启用OSS功能
     * <p>
     * 控制是否加载和初始化OSS相关组件
     * </p>
     */
    private boolean enable = false;

    /**
     * 实现类型标识
     * <p>
     * 用于匹配对应的OssClientFactory，如 aliyun / minio / aws-s3 / tencent-cos
     * </p>
     */
    private String type;

    /**
     * 服务端点地址
     */
    private String endpoint;

    /**
     * 地域
     */
    private String region;

    /**
     * 访问密钥ID
     */
    private String accessKeyId;

    /**
     * 访问密钥Secret
     */
    private String accessKeySecret;

    /**
     * 默认桶名
     * <p>
     * 业务调用未显式指定桶时回退到此桶
     * </p>
     */
    private String defaultBucket;

    /**
     * 是否启用path-style访问
     * <p>
     * MinIO等自建对象存储服务通常需要开启
     * </p>
     */
    private boolean pathStyleAccess = false;

    /**
     * 转换为工厂SPI使用的连接配置模型
     *
     * @return OSS连接配置属性
     */
    public OssProperties toOssProperties() {
        OssProperties properties = new OssProperties();
        properties.setType(type);
        properties.setEndpoint(endpoint);
        properties.setRegion(region);
        properties.setAccessKeyId(accessKeyId);
        properties.setAccessKeySecret(accessKeySecret);
        properties.setDefaultBucket(defaultBucket);
        properties.setPathStyleAccess(pathStyleAccess);
        return properties;
    }
}
