package com.tbs.app.service.oss;

import com.tbs.app.vo.UploadTaskProgressVO;
import com.tbs.app.vo.UploadTaskProgressVO;
import com.tbs.oss.model.ObjectListing;
import com.tbs.oss.model.ObjectMetadata;
import com.tbs.oss.model.PutObjectResult;

import java.util.List;

/**
 * OSS文件服务契约
 * <p>
 * 收敛对象存储的全部业务规则：默认存储键生成策略、附件下载头策略、上传进度采集编排、 缺省桶解析、预签名类型分发与默认有效期、参数校验。 Controller仅做HTTP接入与响应组装，业务实现见实现层
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IOssFileService {

    /**
     * 上传文件
     * <p>
     * 未指定objectKey时按存储策略生成“日期目录/UUID.扩展名”形式键（保留原始扩展名）； 上传内容自动携带附件头，浏览器直接访问对象时触发下载而非内联预览； 指定taskId时登记上传任务并采集写入对象存储的实时进度
     * </p>
     *
     * @param request 上传请求
     * @return 上传结果（含对象键与访问地址）
     */
    PutObjectResult upload(OssUploadRequest request);

    /**
     * 查询上传任务进度
     * <p>
     * 任务不存在时返回RECEIVING状态，表示请求体仍在接收中，尚未进入写入阶段
     * </p>
     *
     * @param taskId 上传任务ID
     * @return 任务进度信息
     */
    UploadTaskProgressVO getUploadProgress(String taskId);

    /**
     * 打开对象内容流
     * <p>
     * bucket为空时使用默认桶；内容流由调用方负责关闭
     * </p>
     *
     * @param bucket    存储桶名（可选）
     * @param objectKey 存储对象键（必填）
     * @return 对象流（含元数据与内容流）
     */
    OssObjectStream openObject(String bucket, String objectKey);

    /**
     * 获取对象元数据
     *
     * @param bucket    存储桶名（可选，缺省使用默认桶）
     * @param objectKey 存储对象键（必填）
     * @return 对象元数据
     */
    ObjectMetadata getMetadata(String bucket, String objectKey);

    /**
     * 判断对象是否存在
     *
     * @param bucket    存储桶名（可选，缺省使用默认桶）
     * @param objectKey 存储对象键（必填）
     * @return 是否存在
     */
    boolean exists(String bucket, String objectKey);

    /**
     * 按前缀分页列举对象
     * <p>
     * 按目录层级列举（公共前缀即目录），支持游标翻页
     * </p>
     *
     * @param prefix 对象键前缀（可选）
     * @param marker 分页游标（可选，取上一页返回的nextMarker）
     * @param bucket 存储桶名（可选，缺省使用默认桶）
     * @return 对象列表（含分页信息）
     */
    ObjectListing listObjects(String prefix, String marker, String bucket);

    /**
     * 生成预签名访问地址
     * <p>
     * type支持DOWNLOAD（下载）与UPLOAD（直传），不区分大小写； expirationSeconds为空或小于等于0时使用默认有效期（1小时）
     * </p>
     *
     * @param objectKey         存储对象键（必填）
     * @param type              地址类型：DOWNLOAD / UPLOAD
     * @param expirationSeconds 有效期（秒，可选）
     * @param bucket            存储桶名（可选，缺省使用默认桶）
     * @return 预签名访问地址
     */
    String generatePresignedUrl(String objectKey, String type, Long expirationSeconds, String bucket);

    /**
     * 删除对象
     *
     * @param objectKey 存储对象键（必填）
     * @param bucket    存储桶名（可选，缺省使用默认桶）
     */
    void deleteObject(String objectKey, String bucket);

    /**
     * 批量删除对象
     *
     * @param bucket     存储桶名（可选，缺省使用默认桶）
     * @param objectKeys 存储对象键列表（必填）
     */
    void deleteObjects(String bucket, List<String> objectKeys);
}
