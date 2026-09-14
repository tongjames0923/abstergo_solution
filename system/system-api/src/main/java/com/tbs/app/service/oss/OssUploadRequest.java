package com.tbs.app.service.oss;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;

/**
 * OSS上传请求模型
 * <p>
 * 服务层上传入参，屏蔽HTTP类型（不感知MultipartFile），
 * 由Controller完成参数组装后传入{@link IOssFileService#upload(OssUploadRequest)}
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@Builder
public class OssUploadRequest {

    /**
     * 上传内容输入流
     */
    private InputStream inputStream;

    /**
     * 内容总字节数
     */
    private long contentLength;

    /**
     * 内容类型（可选）
     */
    private String contentType;

    /**
     * 原始文件名（可选，用于生成默认存储键与附件下载头）
     */
    private String originalFilename;

    /**
     * 存储对象键（可选，缺省由服务端按存储策略自动生成）
     */
    private String objectKey;

    /**
     * 存储桶名（可选，缺省使用默认桶）
     */
    private String bucket;

    /**
     * 上传任务ID（可选，指定后服务端采集写入对象存储的实时进度，供轮询查询）
     */
    private String taskId;
}
