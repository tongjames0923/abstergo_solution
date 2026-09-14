package com.tbs.app.service.oss;

import com.tbs.oss.model.ObjectMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

/**
 * OSS对象流模型
 * <p>
 * 服务层取流入参载体：携带对象元数据与内容流，供调用方组装HTTP响应头后输出；
 * 内容流由调用方负责关闭
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OssObjectStream {

    /**
     * 对象元数据（内容类型、长度等）
     */
    private ObjectMetadata metadata;

    /**
     * 对象存储键（用于解析附件展示文件名）
     */
    private String objectKey;

    /**
     * 对象内容输入流
     */
    private InputStream stream;
}
