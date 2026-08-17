package com.tbs.oss.minio;

import com.tbs.oss.core.OssClient;
import com.tbs.oss.core.OssClientFactory;
import com.tbs.oss.exception.OssException;
import com.tbs.oss.model.OssProperties;
import io.minio.MinioClient;
import org.springframework.stereotype.Component;

/**
 * MinIO实现工厂
 * <p>
 * 注册为Spring Bean后由oss-api的OssConfig按tbs.oss.type自动匹配，
 * 注意本类所在包必须位于com.tbs.oss子包下才能被OssPackAutoRegister扫描到
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class MinioOssClientFactory implements OssClientFactory {

    /** 实现类型标识，对应配置项tbs.oss.type */
    public static final String TYPE = "minio";

    @Override
    public String supportType() {
        return TYPE;
    }

    /**
     * 根据配置创建MinIO客户端
     * <p>
     * MinIO客户端天然采用path-style访问，tbs.oss.path-style-access配置无需特殊处理
     * </p>
     *
     * @param properties OSS连接配置属性
     * @return MinIO实现的OssClient
     */
    @Override
    public OssClient create(OssProperties properties) {
        if (properties.getEndpoint() == null || properties.getEndpoint().isEmpty()) {
            throw new OssException("tbs.oss.endpoint未配置，无法创建MinIO客户端");
        }
        MinioClient.Builder builder = MinioClient.builder()
            .endpoint(properties.getEndpoint())
            .credentials(properties.getAccessKeyId(), properties.getAccessKeySecret());
        if (properties.getRegion() != null && !properties.getRegion().isEmpty()) {
            builder.region(properties.getRegion());
        }
        return new MinioOssClient(builder.build(), properties);
    }
}
