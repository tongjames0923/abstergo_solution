package com.tbs.oss.minio;

import com.tbs.oss.core.OssClient;
import com.tbs.oss.core.ProgressListener;
import com.tbs.oss.exception.OssAccessDeniedException;
import com.tbs.oss.exception.OssBucketNotFoundException;
import com.tbs.oss.exception.OssException;
import com.tbs.oss.exception.OssObjectNotFoundException;
import com.tbs.oss.minio.support.ProgressInputStream;
import com.tbs.oss.model.*;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * OSS门面的MinIO（S3协议）实现
 * <p>
 * 负责将统一契约模型映射为MinIO SDK调用，并将SDK异常统一转换为
 * {@link OssException}异常体系
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class MinioOssClient implements OssClient {

    /** S3用户自定义元数据头前缀 */
    private static final String USER_METADATA_PREFIX = "x-amz-meta-";

    /** 标准HTTP响应头白名单：按对象真实头存储，不添加用户元数据前缀 */
    private static final Set<String> STANDARD_METADATA_HEADERS = new HashSet<>(Arrays.asList(
        "content-disposition", "content-encoding", "content-language", "cache-control", "expires"));

    /** 未知内容长度时的分片大小（MinIO要求不小于5MiB） */
    private static final long DEFAULT_PART_SIZE = 5 * 1024 * 1024L;

    /** 底层MinIO SDK客户端 */
    private final MinioClient client;

    /** 连接配置属性 */
    private final OssProperties properties;

    public MinioOssClient(MinioClient client, OssProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    // ---------- 上传 ----------

    @Override
    public PutObjectResult putObject(PutObjectRequest request) {
        String bucket = resolveBucket(request.getBucketName());
        String key = request.getObjectKey();
        ProgressListener listener = request.getProgressListener();

        InputStream ownedStream = null;
        try {
            InputStream source;
            long size = request.getContentLength();
            if (request.getFile() != null) {
                File file = request.getFile();
                ownedStream = new FileInputStream(file);
                source = ownedStream;
                size = file.length();
            } else if (request.getContent() != null) {
                ownedStream = new ByteArrayInputStream(request.getContent());
                source = ownedStream;
                size = request.getContent().length;
            } else if (request.getInputStream() != null) {
                // 调用方传入的流由调用方负责关闭
                source = request.getInputStream();
            } else {
                throw new OssException("PutObjectRequest未指定任何上传内容(inputStream/file/content)", bucket, key, null);
            }

            InputStream uploadStream = listener != null ? new ProgressInputStream(source, size, listener) : source;
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .stream(uploadStream, size, size > 0 ? -1 : DEFAULT_PART_SIZE);
            if (request.getContentType() != null && !request.getContentType().isEmpty()) {
                builder.contentType(request.getContentType());
            }
            Map<String, String> headers = toUserMetadataHeaders(request.getUserMetadata());
            if (!headers.isEmpty()) {
                builder.headers(headers);
            }

            ObjectWriteResponse response = client.putObject(builder.build());
            if (listener != null) {
                listener.onSuccess();
            }

            PutObjectResult result = new PutObjectResult();
            result.setBucketName(bucket);
            result.setObjectKey(key);
            result.setEtag(response.etag());
            return result;
        } catch (OssException e) {
            notifyError(listener, e);
            throw e;
        } catch (Exception e) {
            OssException translated = translate(e, bucket, key);
            notifyError(listener, translated);
            throw translated;
        } finally {
            closeQuietly(ownedStream);
        }
    }

    @Override
    public PutObjectResult putObject(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        return putObject(PutObjectRequest.builder(objectKey)
            .inputStream(inputStream)
            .contentLength(contentLength)
            .contentType(contentType)
            .build());
    }

    @Override
    public PutObjectResult putObject(String objectKey, File file) {
        return putObject(PutObjectRequest.builder(objectKey).file(file).build());
    }

    @Override
    public PutObjectResult putObject(String objectKey, byte[] content) {
        return putObject(PutObjectRequest.builder(objectKey).content(content).build());
    }

    // ---------- 下载 ----------

    @Override
    public InputStream getObject(String bucketName, String objectKey) {
        String bucket = resolveBucket(bucketName);
        return execute(bucket, objectKey, () -> client.getObject(GetObjectArgs.builder()
            .bucket(bucket)
            .object(objectKey)
            .build()));
    }

    @Override
    public InputStream getObject(String objectKey) {
        return getObject(null, objectKey);
    }

    @Override
    public void getObject(String bucketName, String objectKey, File targetFile) {
        String bucket = resolveBucket(bucketName);
        execute(bucket, objectKey, () -> {
            client.downloadObject(DownloadObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .filename(targetFile.getAbsolutePath())
                .overwrite(true)
                .build());
            return null;
        });
    }

    @Override
    public ObjectMetadata getObjectMetadata(String bucketName, String objectKey) {
        String bucket = resolveBucket(bucketName);
        StatObjectResponse stat = execute(bucket, objectKey, () -> client.statObject(StatObjectArgs.builder()
            .bucket(bucket)
            .object(objectKey)
            .build()));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(stat.size());
        metadata.setContentType(stat.contentType());
        metadata.setEtag(stat.etag());
        if (stat.lastModified() != null) {
            metadata.setLastModified(Date.from(stat.lastModified().toInstant()));
        }
        if (stat.userMetadata() != null) {
            metadata.setUserMetadata(stat.userMetadata());
        }
        return metadata;
    }

    // ---------- 删除/存在性 ----------

    @Override
    public void deleteObject(String bucketName, String objectKey) {
        String bucket = resolveBucket(bucketName);
        execute(bucket, objectKey, () -> {
            client.removeObject(RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .build());
            return null;
        });
    }

    @Override
    public void deleteObject(String objectKey) {
        deleteObject(null, objectKey);
    }

    @Override
    public void deleteObjects(String bucketName, List<String> objectKeys) {
        String bucket = resolveBucket(bucketName);
        if (objectKeys == null || objectKeys.isEmpty()) {
            return;
        }
        execute(bucket, null, () -> {
            List<DeleteObject> deletes = objectKeys.stream().map(DeleteObject::new).collect(Collectors.toList());
            Iterable<Result<DeleteError>> results = client.removeObjects(RemoveObjectsArgs.builder()
                .bucket(bucket)
                .objects(deletes)
                .build());
            // 必须遍历结果才会真正触发删除
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                throw new OssException("批量删除失败: " + error.message(), error.bucketName(), error.objectName(), null);
            }
            return null;
        });
    }

    @Override
    public boolean doesObjectExist(String bucketName, String objectKey) {
        String bucket = resolveBucket(bucketName);
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(objectKey).build());
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw translate(e, bucket, objectKey);
        } catch (Exception e) {
            throw translate(e, bucket, objectKey);
        }
    }

    // ---------- 复制/移动 ----------

    @Override
    public void copyObject(CopyObjectRequest request) {
        String sourceBucket = resolveBucket(request.getSourceBucketName());
        String targetBucket = resolveBucket(request.getTargetBucketName());
        execute(targetBucket, request.getTargetObjectKey(), () -> {
            client.copyObject(CopyObjectArgs.builder()
                .bucket(targetBucket)
                .object(request.getTargetObjectKey())
                .source(CopySource.builder()
                    .bucket(sourceBucket)
                    .object(request.getSourceObjectKey())
                    .build())
                .build());
            return null;
        });
    }

    @Override
    public void moveObject(CopyObjectRequest request) {
        copyObject(request);
        deleteObject(request.getSourceBucketName(), request.getSourceObjectKey());
    }

    // ---------- 列表 ----------

    @Override
    public ObjectListing listObjects(ListObjectsRequest request) {
        String bucket = resolveBucket(request.getBucketName());
        int maxKeys = request.getMaxKeys() > 0 ? request.getMaxKeys() : 100;
        return execute(bucket, null, () -> {
            ListObjectsArgs.Builder builder = ListObjectsArgs.builder()
                .bucket(bucket)
                // 多取一条用于判断是否还有下一页
                .maxKeys(maxKeys + 1)
                .recursive(request.getDelimiter() == null);
            if (request.getPrefix() != null && !request.getPrefix().isEmpty()) {
                builder.prefix(request.getPrefix());
            }
            if (request.getDelimiter() != null && !request.getDelimiter().isEmpty()) {
                builder.delimiter(request.getDelimiter());
            }
            if (request.getMarker() != null && !request.getMarker().isEmpty()) {
                builder.startAfter(request.getMarker());
            }

            ObjectListing listing = new ObjectListing();
            List<ObjectInfo> objects = new ArrayList<>();
            // 目录（公共前缀）与文件条目共同占用maxKeys额度，否则两者叠加会超出单页容量
            int totalEntries = 0;
            boolean truncated = false;
            for (Result<Item> result : client.listObjects(builder.build())) {
                if (totalEntries >= maxKeys) {
                    truncated = true;
                    break;
                }
                Item item = result.get();
                if (item.isDir()) {
                    listing.getCommonPrefixes().add(item.objectName());
                } else {
                    ObjectInfo info = new ObjectInfo();
                    info.setBucketName(bucket);
                    info.setObjectKey(item.objectName());
                    info.setContentLength(item.size());
                    info.setEtag(item.etag());
                    if (item.lastModified() != null) {
                        info.setLastModified(Date.from(item.lastModified().toInstant()));
                    }
                    objects.add(info);
                }
                totalEntries++;
            }

            listing.setObjects(objects);
            listing.setTruncated(truncated);
            listing.setNextMarker(truncated ? nextMarkerOf(objects, listing.getCommonPrefixes()) : null);
            return listing;
        });
    }

    // ---------- 预签名 URL ----------

    @Override
    public String generatePresignedUrl(PresignedUrlRequest request) {
        String bucket = resolveBucket(request.getBucketName());
        Method method = request.getType() == PresignedUrlType.UPLOAD ? Method.PUT : Method.GET;
        return execute(bucket, request.getObjectKey(),
            () -> client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(method)
                .bucket(bucket)
                .object(request.getObjectKey())
                .expiry((int) request.getExpirationSeconds(), TimeUnit.SECONDS)
                .build()));
    }

    @Override
    public String generatePresignedUrl(String objectKey, long expirationSeconds) {
        return generatePresignedUrl(PresignedUrlRequest.download(objectKey)
            .expirationSeconds(expirationSeconds)
            .build());
    }

    @Override
    public String getObjectUrl(String bucketName, String objectKey) {
        String bucket = resolveBucket(bucketName);
        String endpoint = properties.getEndpoint();
        String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return base + "/" + bucket + "/" + objectKey;
    }

    // ---------- 桶管理 ----------

    @Override
    public void createBucket(String bucketName) {
        execute(bucketName, null, () -> {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            return null;
        });
    }

    @Override
    public void deleteBucket(String bucketName) {
        execute(bucketName, null, () -> {
            client.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            return null;
        });
    }

    @Override
    public boolean doesBucketExist(String bucketName) {
        return execute(bucketName, null,
            () -> client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()));
    }

    @Override
    public List<Bucket> listBuckets() {
        return execute(null, null, () -> client.listBuckets().stream().map(source -> {
            Bucket bucket = new Bucket();
            bucket.setName(source.name());
            if (source.creationDate() != null) {
                bucket.setCreationDate(Date.from(source.creationDate().toInstant()));
            }
            return bucket;
        }).collect(Collectors.toList()));
    }

    // ---------- OssClient 元信息 ----------

    @Override
    public String getType() {
        return MinioOssClientFactory.TYPE;
    }

    @Override
    public String getDefaultBucket() {
        return properties.getDefaultBucket();
    }

    @Override
    public OssProperties getProperties() {
        return properties;
    }

    @Override
    public void close() {
        // MinIO SDK 8.5.x的客户端未暴露关闭方法，
        // 底层OkHttp连接池由空闲超时自动回收，此处无需处理
    }

    // ---------- 内部支撑 ----------

    /**
     * 计算下一页游标
     * <p>
     * 列举结果按对象键字典序返回，取本页最后一条文件与最后一个公共前缀中字典序较大者作为 startAfter 游标
     * </p>
     *
     * @param objects        本页对象列表
     * @param commonPrefixes 本页公共前缀列表
     * @return 下一页游标，本页为空时返回null
     */
    private String nextMarkerOf(List<ObjectInfo> objects, List<String> commonPrefixes) {
        String lastObject = objects.isEmpty() ? null : objects.get(objects.size() - 1).getObjectKey();
        String lastPrefix = commonPrefixes.isEmpty() ? null : commonPrefixes.get(commonPrefixes.size() - 1);
        if (lastObject == null) {
            return lastPrefix;
        }
        if (lastPrefix == null) {
            return lastObject;
        }
        return lastObject.compareTo(lastPrefix) >= 0 ? lastObject : lastPrefix;
    }

    /**
     * 解析桶名，未指定时回退到配置的默认桶
     *
     * @param bucketName 调用方指定的桶名，可为null
     * @return 最终使用的桶名
     */
    private String resolveBucket(String bucketName) {
        String bucket = bucketName != null && !bucketName.isEmpty() ? bucketName : properties.getDefaultBucket();
        if (bucket == null || bucket.isEmpty()) {
            throw new OssException("未指定桶名且未配置tbs.oss.default-bucket");
        }
        return bucket;
    }

    /**
     * 将用户自定义元数据转换为S3协议要求的请求头
     * <p>
     * 标准HTTP响应头（如Content-Disposition）直接作为对象真实头存储，
     * 浏览器直接访问对象时按其语义生效；其余补充x-amz-meta-前缀作为用户元数据
     * </p>
     *
     * @param userMetadata 用户自定义元数据，可为null
     * @return 请求头映射
     */
    private Map<String, String> toUserMetadataHeaders(Map<String, String> userMetadata) {
        Map<String, String> headers = new HashMap<>();
        if (userMetadata == null || userMetadata.isEmpty()) {
            return headers;
        }
        userMetadata.forEach((name, value) -> {
            String lower = name.toLowerCase();
            if (STANDARD_METADATA_HEADERS.contains(lower)) {
                headers.put(name, value);
            } else {
                String key = lower.startsWith(USER_METADATA_PREFIX) ? name : USER_METADATA_PREFIX + name;
                headers.put(key, value);
            }
        });
        return headers;
    }

    /**
     * 通知进度监听器传输失败
     *
     * @param listener 进度监听器，可为null
     * @param e        失败异常
     */
    private void notifyError(ProgressListener listener, Exception e) {
        if (listener != null) {
            listener.onError(e);
        }
    }

    /**
     * 安静关闭流，忽略异常
     *
     * @param stream 待关闭的流，可为null
     */
    private void closeQuietly(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
                // 关闭失败不影响主流程
            }
        }
    }

    /**
     * 执行SDK操作并统一转换异常
     *
     * @param bucket    操作涉及的桶名，用于异常上下文
     * @param key       操作涉及的对象Key，用于异常上下文
     * @param operation SDK操作
     * @param <T>       返回值类型
     * @return SDK操作结果
     */
    private <T> T execute(String bucket, String key, SdkOperation<T> operation) {
        try {
            return operation.execute();
        } catch (Exception e) {
            throw translate(e, bucket, key);
        }
    }

    /**
     * 将MinIO SDK异常转换为统一异常体系
     *
     * @param e      原始异常
     * @param bucket 操作涉及的桶名
     * @param key    操作涉及的对象Key
     * @return 统一异常
     */
    private OssException translate(Exception e, String bucket, String key) {
        if (e instanceof ErrorResponseException) {
            String code = ((ErrorResponseException) e).errorResponse().code();
            if ("NoSuchKey".equals(code)) {
                return new OssObjectNotFoundException(bucket, key, e);
            }
            if ("NoSuchBucket".equals(code)) {
                return new OssBucketNotFoundException(bucket, e);
            }
            if ("AccessDenied".equals(code) || "InvalidAccessKeyId".equals(code) || "SignatureDoesNotMatch".equals(code)) {
                return new OssAccessDeniedException("OSS访问被拒绝: " + code, e);
            }
            return new OssException("OSS操作失败: " + code, bucket, key, e);
        }
        if (e instanceof IllegalArgumentException) {
            return new OssException("OSS参数错误: " + e.getMessage(), bucket, key, e);
        }
        return new OssException("OSS操作异常: " + e.getMessage(), bucket, key, e);
    }

    /**
     * SDK操作函数式接口，允许抛出受检异常
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    private interface SdkOperation<T> {
        T execute() throws Exception;
    }
}
