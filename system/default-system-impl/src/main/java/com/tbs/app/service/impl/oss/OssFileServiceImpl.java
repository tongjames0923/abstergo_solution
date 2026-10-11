package com.tbs.app.service.impl.oss;

import cn.hutool.core.date.DateUtil;
import com.tbs.app.constant.FileProcessI18nConstant;
import com.tbs.app.service.oss.IOssFileService;
import com.tbs.app.service.oss.OssObjectStream;
import com.tbs.app.service.oss.OssUploadRequest;
import com.tbs.app.service.oss.UploadTaskProgressManager;
import com.tbs.app.vo.UploadTaskProgressVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.oss.core.OssClient;
import com.tbs.oss.core.ProgressListener;
import com.tbs.oss.model.ListObjectsRequest;
import com.tbs.oss.model.ObjectListing;
import com.tbs.oss.model.ObjectMetadata;
import com.tbs.oss.model.PresignedUrlRequest;
import com.tbs.oss.model.PutObjectRequest;
import com.tbs.oss.model.PutObjectResult;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * OSS文件服务实现
 * <p>
 * 收敛对象存储的全部业务规则：默认存储键生成（日期目录/UUID.扩展名）、附件下载头策略、
 * 上传进度采集编排、缺省桶解析、预签名类型分发与默认有效期、参数校验；
 * 对{@link OssClient}的直接依赖收敛在本实现内
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Service
public class OssFileServiceImpl implements IOssFileService {

    /**
     * 预签名地址默认有效期（秒）
     */
    private static final long DEFAULT_PRESIGNED_EXPIRE_SECONDS = 3600L;

    /**
     * 列举对象默认最大条数
     */
    private static final int DEFAULT_LIST_MAX_KEYS = 100;

    /**
     * 对象存储门面
     */
    @Resource
    private OssClient ossClient;

    /**
     * 上传任务进度管理器
     */
    @Resource
    private UploadTaskProgressManager uploadTaskProgressManager;

    /**
     * 上传文件
     * <p>
     * 完成参数校验、默认键生成、附件头写入与进度采集编排后委托对象存储门面执行上传
     * </p>
     *
     * @param request 上传请求
     * @return 上传结果（含对象键与访问地址）
     */
    @Override
    public PutObjectResult upload(OssUploadRequest request) {
        AssertUtil.isTrue(request == null || request.getInputStream() == null)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n(FileProcessI18nConstant.OSS_FILE_EMPTY).throwIt();
        String key = StrUtil.isEmpty(request.getObjectKey()) ? generateObjectKey(request.getOriginalFilename())
            : request.getObjectKey();
        boolean trackProgress = !StrUtil.isEmpty(request.getTaskId());
        if (trackProgress) {
            uploadTaskProgressManager.register(request.getTaskId(), request.getContentLength());
        }
        PutObjectRequest.Builder builder = PutObjectRequest.builder(key).inputStream(request.getInputStream())
            .contentLength(request.getContentLength()).contentType(request.getContentType());
        applyAttachmentHeader(builder, key, request.getOriginalFilename());
        if (!StrUtil.isEmpty(request.getBucket())) {
            builder.bucketName(request.getBucket());
        }
        if (trackProgress) {
            attachProgressListener(builder, request.getTaskId());
        }
        return ossClient.putObject(builder.build());
    }

    /**
     * 查询上传任务进度
     *
     * @param taskId 上传任务ID
     * @return 任务进度信息，任务不存在时返回RECEIVING状态
     */
    @Override
    public UploadTaskProgressVO getUploadProgress(String taskId) {
        AssertUtil.isTrue(StrUtil.isEmpty(taskId)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(FileProcessI18nConstant.OSS_TASK_ID_EMPTY).throwIt();
        UploadTaskProgressVO progress = uploadTaskProgressManager.get(taskId);
        if (progress == null) {
            progress = new UploadTaskProgressVO();
            progress.setTaskId(taskId);
            progress.setStatus(UploadTaskProgressVO.STATUS_RECEIVING);
        }
        return progress;
    }

    /**
     * 打开对象内容流
     *
     * @param bucket    存储桶名（可选）
     * @param objectKey 存储对象键（必填）
     * @return 对象流（含元数据与内容流）
     */
    @Override
    public OssObjectStream openObject(String bucket, String objectKey) {
        assertObjectKeyNotEmpty(objectKey);
        String resolvedBucket = resolveBucket(bucket);
        ObjectMetadata metadata = ossClient.getObjectMetadata(resolvedBucket, objectKey);
        return new OssObjectStream(metadata, objectKey, ossClient.getObject(resolvedBucket, objectKey));
    }

    /**
     * 获取对象元数据
     *
     * @param bucket    存储桶名（可选，缺省使用默认桶）
     * @param objectKey 存储对象键（必填）
     * @return 对象元数据
     */
    @Override
    public ObjectMetadata getMetadata(String bucket, String objectKey) {
        assertObjectKeyNotEmpty(objectKey);
        return ossClient.getObjectMetadata(resolveBucket(bucket), objectKey);
    }

    /**
     * 判断对象是否存在
     *
     * @param bucket    存储桶名（可选，缺省使用默认桶）
     * @param objectKey 存储对象键（必填）
     * @return 是否存在
     */
    @Override
    public boolean exists(String bucket, String objectKey) {
        assertObjectKeyNotEmpty(objectKey);
        return ossClient.doesObjectExist(resolveBucket(bucket), objectKey);
    }

    /**
     * 按前缀分页列举对象
     *
     * @param prefix 对象键前缀（可选）
     * @param marker 分页游标（可选）
     * @param bucket 存储桶名（可选，缺省使用默认桶）
     * @return 对象列表（含分页信息）
     */
    @Override
    public ObjectListing listObjects(String prefix, String marker, String bucket) {
        ListObjectsRequest.Builder builder = ListObjectsRequest.builder().prefix(prefix).marker(marker)
            .maxKeys(DEFAULT_LIST_MAX_KEYS).delimiter("/");
        if (!StrUtil.isEmpty(bucket)) {
            builder.bucketName(bucket);
        }
        return ossClient.listObjects(builder.build());
    }

    /**
     * 生成预签名访问地址
     *
     * @param objectKey         存储对象键（必填）
     * @param type              地址类型：DOWNLOAD / UPLOAD
     * @param expirationSeconds 有效期（秒，可选）
     * @param bucket            存储桶名（可选，缺省使用默认桶）
     * @return 预签名访问地址
     */
    @Override
    public String generatePresignedUrl(String objectKey, String type, Long expirationSeconds, String bucket) {
        assertObjectKeyNotEmpty(objectKey);
        PresignedUrlRequest.Builder builder = "UPLOAD".equalsIgnoreCase(type) ? PresignedUrlRequest.upload(objectKey)
            : PresignedUrlRequest.download(objectKey);
        builder.expirationSeconds(
            expirationSeconds == null || expirationSeconds <= 0 ? DEFAULT_PRESIGNED_EXPIRE_SECONDS : expirationSeconds);
        if (!StrUtil.isEmpty(bucket)) {
            builder.bucketName(bucket);
        }
        return ossClient.generatePresignedUrl(builder.build());
    }

    /**
     * 删除对象
     *
     * @param objectKey 存储对象键（必填）
     * @param bucket    存储桶名（可选，缺省使用默认桶）
     */
    @Override
    public void deleteObject(String objectKey, String bucket) {
        assertObjectKeyNotEmpty(objectKey);
        if (StrUtil.isEmpty(bucket)) {
            ossClient.deleteObject(objectKey);
        } else {
            ossClient.deleteObject(bucket, objectKey);
        }
    }

    /**
     * 批量删除对象
     *
     * @param bucket     存储桶名（可选，缺省使用默认桶）
     * @param objectKeys 存储对象键列表（必填）
     */
    @Override
    public void deleteObjects(String bucket, List<String> objectKeys) {
        AssertUtil.isTrue(objectKeys == null || objectKeys.isEmpty()).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(FileProcessI18nConstant.OSS_OBJECT_KEY_EMPTY).throwIt();
        ossClient.deleteObjects(resolveBucket(bucket), objectKeys);
    }

    /**
     * 写入附件下载头
     * <p>
     * 存储时携带Content-Disposition附件元数据，浏览器直接访问对象（如预签名地址）时
     * 触发下载而非内联预览
     * </p>
     *
     * @param builder          上传请求构建器
     * @param key              存储对象键
     * @param originalFilename 原始文件名（可选，缺省使用对象键）
     */
    private void applyAttachmentHeader(PutObjectRequest.Builder builder, String key, String originalFilename) {
        String fileName = StrUtil.isEmpty(originalFilename) ? key : originalFilename;
        builder.userMetadata(Collections.singletonMap("Content-Disposition", "attachment;filename=" + encodeFileName(fileName)));
    }

    /**
     * URL编码文件名
     *
     * @param fileName 原始文件名
     * @return URL编码后的文件名（空格转%20）
     */
    private String encodeFileName(String fileName) {
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * 挂载上传进度监听器
     * <p>
     * 采集写入对象存储的真实字节数（MinIO实现按64KiB步长驱动回调）并回写任务进度
     * </p>
     *
     * @param builder 上传请求构建器
     * @param taskId  上传任务ID
     */
    private void attachProgressListener(PutObjectRequest.Builder builder, String taskId) {
        builder.progressListener(new ProgressListener.Adapter() {

            /**
             * 处理写入进度回调
             *
             * @param transferredBytes 已写入字节数
             * @param totalBytes       总字节数
             */
            @Override
            public void onProgress(long transferredBytes, long totalBytes) {
                uploadTaskProgressManager.updateTransferred(taskId, transferredBytes);
            }

            /**
             * 处理上传成功回调
             */
            @Override
            public void onSuccess() {
                uploadTaskProgressManager.markSuccess(taskId);
            }

            /**
             * 处理上传失败回调
             *
             * @param e 失败原因
             */
            @Override
            public void onError(Exception e) {
                uploadTaskProgressManager.markFailed(taskId);
            }
        });
    }

    /**
     * 生成上传对象的默认存储键
     * <p>
     * 格式为“yyyyMMdd/UUID.扩展名”，保留原始文件扩展名
     * </p>
     *
     * @param originalFilename 原始文件名（可选）
     * @return 存储对象键
     */
    private String generateObjectKey(String originalFilename) {
        String date = DateUtil.format(new Date(), "yyyyMMdd");
        String suffix = "";
        if (!StrUtil.isEmpty(originalFilename) && originalFilename.lastIndexOf('.') > 0) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return date + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;
    }

    /**
     * 解析实际使用的存储桶名
     *
     * @param bucket 调用方传入的桶名（可为空）
     * @return 缺省时使用默认桶的最终桶名
     */
    private String resolveBucket(String bucket) {
        return StrUtil.isEmpty(bucket) ? ossClient.getDefaultBucket() : bucket;
    }

    /**
     * 断言对象键非空
     *
     * @param objectKey 存储对象键
     */
    private void assertObjectKeyNotEmpty(String objectKey) {
        AssertUtil.isTrue(StrUtil.isEmpty(objectKey)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(FileProcessI18nConstant.OSS_OBJECT_KEY_EMPTY).throwIt();
    }
}
