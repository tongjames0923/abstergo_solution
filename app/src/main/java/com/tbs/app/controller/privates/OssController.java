package com.tbs.app.controller.privates;

import cn.hutool.core.date.DateUtil;
import com.tbs.app.annotation.NoLog;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.oss.core.OssClient;
import com.tbs.oss.model.*;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 对象存储控制器
 * <p>
 * 该控制器基于统一OSS门面提供文件上传、下载、删除、列举、预签名地址等能力， 底层由{@link OssClient}适配具体的对象存储实现（如MinIO）
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("private/oss")
@Tag(name = "对象存储接口", description = "文件上传、下载、删除、列举及预签名地址等操作接口")
@NoLog
public class OssController extends BaseController {

    /**
     * 预签名地址默认有效期（秒）
     */
    private static final long DEFAULT_PRESIGNED_EXPIRE_SECONDS = 3600L;

    /**
     * 列举对象默认最大条数
     */
    private static final int DEFAULT_LIST_MAX_KEYS = 100;

    @Resource
    private OssClient ossClient;

    /**
     * 上传文件
     * <p>
     * 未指定objectKey时自动生成“日期目录/UUID.扩展名”形式的存储路径， 避免直接暴露原始文件名
     * </p>
     *
     * @param file      上传的文件
     * @param objectKey 存储对象键（可选）
     * @param bucket    存储桶（可选，缺省使用默认桶）
     * @return 上传结果，包含对象键与访问地址
     */
    @PostMapping("upload")
    @AjaxResponseType(PutObjectResult.class)
    @Operation(summary = "上传文件", description = "上传文件到对象存储，可指定存储对象键与存储桶")
    @ApiResponse(responseCode = "200", description = "成功上传文件",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult upload(
        @Parameter(name = "file", description = "上传的文件", required = true) @RequestParam("file") MultipartFile file,
        @Parameter(name = "objectKey", description = "存储对象键，缺省自动生成") @RequestParam(value = "objectKey",
            required = false) String objectKey,
        @Parameter(name = "bucket", description = "存储桶，缺省使用默认桶") @RequestParam(value = "bucket",
            required = false) String bucket) throws IOException {
        AssertUtil.isTrue(file == null || file.isEmpty()).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n("oss.file.empty").throwIt();
        String key = StrUtil.isEmpty(objectKey) ? generateObjectKey(file.getOriginalFilename()) : objectKey;
        PutObjectRequest.Builder builder =
            PutObjectRequest.builder(key).inputStream(file.getInputStream()).contentLength(file.getSize())
                .contentType(file.getContentType());
        if (!StrUtil.isEmpty(bucket)) {
            builder.bucketName(bucket);
        }
        PutObjectResult result = ossClient.putObject(builder.build());
        return successResult(result);
    }

    /**
     * 下载文件
     * <p>
     * 以附件形式输出对象内容，文件名取对象键的最后一段并做URL编码
     * </p>
     *
     * @param objectKey 存储对象键
     * @param bucket    存储桶（可选，缺省使用默认桶）
     * @param response  HTTP响应
     */
    @GetMapping("download")
    @Operation(summary = "下载文件", description = "以附件形式下载指定的存储对象")
    @ApiResponse(responseCode = "200", description = "成功下载文件")
    @PreAuthorize("@preAuthUtils.accountActived")
    public void download(@Parameter(name = "objectKey", description = "存储对象键", required = true) @RequestParam(
            "objectKey") String objectKey,
        @Parameter(name = "bucket", description = "存储桶，缺省使用默认桶") @RequestParam(value = "bucket",
            required = false) String bucket, HttpServletResponse response) throws IOException {
        AssertUtil.isTrue(StrUtil.isEmpty(objectKey)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n("oss.object.key.empty").throwIt();
        String resolvedBucket = StrUtil.isEmpty(bucket) ? ossClient.getDefaultBucket() : bucket;
        ObjectMetadata metadata = ossClient.getObjectMetadata(resolvedBucket, objectKey);
        response.setContentType(StrUtil.isEmpty(metadata.getContentType()) ? MediaType.APPLICATION_OCTET_STREAM_VALUE
            : metadata.getContentType());
        if (metadata.getContentLength() > 0) {
            response.setContentLengthLong(metadata.getContentLength());
        }
        String fileName = objectKey.contains("/") ? objectKey.substring(objectKey.lastIndexOf('/') + 1) : objectKey;
        response.setHeader("Content-Disposition",
            "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20"));
        try (InputStream inputStream = ossClient.getObject(resolvedBucket, objectKey);
            OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        }
    }

    /**
     * 获取对象元数据
     *
     * @param objectKey 存储对象键
     * @param bucket    存储桶（可选，缺省使用默认桶）
     * @return 对象元数据
     */
    @GetMapping("metadata")
    @AjaxResponseType(ObjectMetadata.class)
    @Operation(summary = "获取对象元数据", description = "获取指定存储对象的大小、类型、修改时间等元数据")
    @ApiResponse(responseCode = "200", description = "成功获取对象元数据",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult metadata(
        @Parameter(name = "objectKey", description = "存储对象键", required = true) @RequestParam(
            "objectKey") String objectKey,
        @Parameter(name = "bucket", description = "存储桶，缺省使用默认桶") @RequestParam(value = "bucket",
            required = false) String bucket) {
        AssertUtil.isTrue(StrUtil.isEmpty(objectKey)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n("oss.object.key.empty").throwIt();
        String resolvedBucket = StrUtil.isEmpty(bucket) ? ossClient.getDefaultBucket() : bucket;
        return successResult(ossClient.getObjectMetadata(resolvedBucket, objectKey));
    }

    /**
     * 判断对象是否存在
     *
     * @param objectKey 存储对象键
     * @param bucket    存储桶（可选，缺省使用默认桶）
     * @return 是否存在
     */
    @GetMapping("exist")
    @AjaxResponseType(Boolean.class)
    @Operation(summary = "判断对象是否存在", description = "判断指定存储对象是否存在")
    @ApiResponse(responseCode = "200", description = "成功判断对象是否存在",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult exist(@Parameter(name = "objectKey", description = "存储对象键", required = true) @RequestParam(
            "objectKey") String objectKey,
        @Parameter(name = "bucket", description = "存储桶，缺省使用默认桶") @RequestParam(value = "bucket",
            required = false) String bucket) {
        AssertUtil.isTrue(StrUtil.isEmpty(objectKey)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n("oss.object.key.empty").throwIt();
        String resolvedBucket = StrUtil.isEmpty(bucket) ? ossClient.getDefaultBucket() : bucket;
        return successResult(ossClient.doesObjectExist(resolvedBucket, objectKey));
    }

    /**
     * 列举对象
     *
     * @param prefix 对象键前缀（可选）
     * @param marker 分页游标（可选，取上一页返回的nextMarker）
     * @param bucket 存储桶（可选，缺省使用默认桶）
     * @return 对象列表（含分页信息）
     */
    @GetMapping("list")
    @AjaxResponseType(ObjectListing.class)
    @Operation(summary = "列举对象", description = "按前缀分页列举存储对象，支持游标翻页")
    @ApiResponse(responseCode = "200", description = "成功列举对象",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult list(@Parameter(name = "prefix", description = "对象键前缀") @RequestParam(value = "prefix",
            required = false) String prefix,
        @Parameter(name = "marker", description = "分页游标，取上一页返回的nextMarker") @RequestParam(value = "marker",
            required = false) String marker,
        @Parameter(name = "bucket", description = "存储桶，缺省使用默认桶") @RequestParam(value = "bucket",
            required = false) String bucket) {
        ListObjectsRequest.Builder builder =
            ListObjectsRequest.builder().prefix(prefix).marker(marker).maxKeys(DEFAULT_LIST_MAX_KEYS);
        if (!StrUtil.isEmpty(bucket)) {
            builder.bucketName(bucket);
        }
        return successResult(ossClient.listObjects(builder.build()));
    }

    /**
     * 生成预签名访问地址
     * <p>
     * type取值：DOWNLOAD（下载）、UPLOAD（直传）
     * </p>
     *
     * @param objectKey         存储对象键
     * @param type              地址类型
     * @param expirationSeconds 有效期（秒，可选）
     * @param bucket            存储桶（可选，缺省使用默认桶）
     * @return 预签名访问地址
     */
    @GetMapping("presigned-url")
    @AjaxResponseType(String.class)
    @Operation(summary = "生成预签名访问地址", description = "生成对象在有效期内的预签名访问地址，支持下载与直传")
    @ApiResponse(responseCode = "200", description = "成功生成预签名访问地址",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult presignedUrl(
        @Parameter(name = "objectKey", description = "存储对象键", required = true) @RequestParam(
            "objectKey") String objectKey,
        @Parameter(name = "type", description = "地址类型：DOWNLOAD下载 / UPLOAD直传", required = true) @RequestParam(
            value = "type", defaultValue = "DOWNLOAD") String type,
        @Parameter(name = "expirationSeconds", description = "有效期（秒）") @RequestParam(value = "expirationSeconds",
            required = false) Long expirationSeconds,
        @Parameter(name = "bucket", description = "存储桶，缺省使用默认桶") @RequestParam(value = "bucket",
            required = false) String bucket) {
        AssertUtil.isTrue(StrUtil.isEmpty(objectKey)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n("oss.object.key.empty").throwIt();
        PresignedUrlRequest.Builder builder = "UPLOAD".equalsIgnoreCase(type) ? PresignedUrlRequest.upload(objectKey)
            : PresignedUrlRequest.download(objectKey);
        builder.expirationSeconds(
            expirationSeconds == null || expirationSeconds <= 0 ? DEFAULT_PRESIGNED_EXPIRE_SECONDS : expirationSeconds);
        if (!StrUtil.isEmpty(bucket)) {
            builder.bucketName(bucket);
        }
        return successResult(ossClient.generatePresignedUrl(builder.build()));
    }

    /**
     * 删除对象
     *
     * @param objectKey 存储对象键
     * @param bucket    存储桶（可选，缺省使用默认桶）
     */
    @DeleteMapping
    @AjaxResponseType(void.class)
    @Operation(summary = "删除对象", description = "删除指定的存储对象")
    @ApiResponse(responseCode = "200", description = "成功删除对象",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult delete(@Parameter(name = "objectKey", description = "存储对象键", required = true) @RequestParam(
            "objectKey") String objectKey,
        @Parameter(name = "bucket", description = "存储桶，缺省使用默认桶") @RequestParam(value = "bucket",
            required = false) String bucket) {
        AssertUtil.isTrue(StrUtil.isEmpty(objectKey)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n("oss.object.key.empty").throwIt();
        if (StrUtil.isEmpty(bucket)) {
            ossClient.deleteObject(objectKey);
        } else {
            ossClient.deleteObject(bucket, objectKey);
        }
        return successResult();
    }

    /**
     * 批量删除对象
     *
     * @param objectKeys 存储对象键列表
     * @param bucket     存储桶（可选，缺省使用默认桶）
     */
    @DeleteMapping("batch")
    @AjaxResponseType(void.class)
    @Operation(summary = "批量删除对象", description = "批量删除指定的存储对象")
    @ApiResponse(responseCode = "200", description = "成功批量删除对象",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult deleteBatch(@Parameter(name = "objectKeys", description = "存储对象键列表",
            required = true) @RequestBody List<String> objectKeys,
        @Parameter(name = "bucket", description = "存储桶，缺省使用默认桶") @RequestParam(value = "bucket",
            required = false) String bucket) {
        AssertUtil.isTrue(objectKeys == null || objectKeys.isEmpty()).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n("oss.object.key.empty").throwIt();
        String resolvedBucket = StrUtil.isEmpty(bucket) ? ossClient.getDefaultBucket() : bucket;
        ossClient.deleteObjects(resolvedBucket, objectKeys);
        return successResult();
    }

    /**
     * 生成上传对象的默认存储键
     * <p>
     * 格式为“yyyyMMdd/UUID.扩展名”，保留原始文件扩展名
     * </p>
     *
     * @param originalFilename 原始文件名
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
     * 获取业务名称
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "对象存储控制器";
    }
}
