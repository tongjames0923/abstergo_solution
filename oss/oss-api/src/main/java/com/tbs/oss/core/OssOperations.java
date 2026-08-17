package com.tbs.oss.core;

import com.tbs.oss.model.CopyObjectRequest;
import com.tbs.oss.model.ListObjectsRequest;
import com.tbs.oss.model.ObjectListing;
import com.tbs.oss.model.ObjectMetadata;
import com.tbs.oss.model.PresignedUrlRequest;
import com.tbs.oss.model.PutObjectRequest;
import com.tbs.oss.model.PutObjectResult;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * 对象级操作契约，所有方法均可指定桶名；
 * 桶名为 null 时由实现回退到配置的默认桶。
 * <p>
 * 实现约定：
 * 1. 所有第三方 SDK 异常必须转换为 {@link com.tbs.oss.exception.OssException} 及其子类；
 * 2. 上传流由调用方负责关闭，实现不得提前关闭调用方传入的流之外的资源；
 * 3. getObject 返回的流由调用方负责关闭。
 */
public interface OssOperations {

    // ---------- 上传 ----------

    /** 上传对象，返回上传结果 */
    PutObjectResult putObject(PutObjectRequest request);

    /** 上传流到默认桶，contentLength 为 -1 表示未知（实现需自行处理，如缓冲或分片） */
    PutObjectResult putObject(String objectKey, InputStream inputStream, long contentLength, String contentType);

    /** 上传本地文件到默认桶 */
    PutObjectResult putObject(String objectKey, File file);

    /** 上传字节内容到默认桶 */
    PutObjectResult putObject(String objectKey, byte[] content);

    // ---------- 下载 ----------

    /** 下载对象，返回输入流，调用方负责关闭 */
    InputStream getObject(String bucketName, String objectKey);

    /** 从默认桶下载对象，返回输入流 */
    InputStream getObject(String objectKey);

    /** 下载对象到本地文件 */
    void getObject(String bucketName, String objectKey, File targetFile);

    /** 获取对象元数据 */
    ObjectMetadata getObjectMetadata(String bucketName, String objectKey);

    // ---------- 删除/存在性 ----------

    /** 删除对象 */
    void deleteObject(String bucketName, String objectKey);

    /** 从默认桶删除对象 */
    void deleteObject(String objectKey);

    /** 批量删除对象 */
    void deleteObjects(String bucketName, List<String> objectKeys);

    /** 判断对象是否存在 */
    boolean doesObjectExist(String bucketName, String objectKey);

    // ---------- 复制/移动 ----------

    /** 复制对象 */
    void copyObject(CopyObjectRequest request);

    /** 移动对象（复制+删除源对象） */
    void moveObject(CopyObjectRequest request);

    // ---------- 列表 ----------

    /** 列出对象，支持前缀、分隔符、marker 分页 */
    ObjectListing listObjects(ListObjectsRequest request);

    // ---------- 预签名 URL ----------

    /** 生成预签名URL（下载或上传） */
    String generatePresignedUrl(PresignedUrlRequest request);

    /** 生成默认桶的下载预签名URL，有效期秒 */
    String generatePresignedUrl(String objectKey, long expirationSeconds);

    /** 获取对象访问URL（桶为公开读时有效，否则由实现决定行为） */
    String getObjectUrl(String bucketName, String objectKey);
}
