package com.tbs.app.controller.privates;

import com.tbs.app.service.oss.IOssFileService;
import com.tbs.app.service.oss.OssObjectStream;
import com.tbs.app.service.oss.OssUploadRequest;
import com.tbs.app.vo.UploadTaskProgressVO;
import com.tbs.common.util.base.StrUtil;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.logging.annotation.NoLog;
import com.tbs.oss.model.ObjectListing;
import com.tbs.oss.model.ObjectMetadata;
import com.tbs.oss.model.PutObjectResult;
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
import java.util.List;

/**
 * 对象存储控制器
 * <p>
 * 对象存储HTTP接入层：仅做参数绑定、调用{@link IOssFileService}与响应组装；
 * 上传策略（默认键生成/附件头/进度采集）、缺省桶解析、预签名规则等业务逻辑均在服务实现层
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("private/oss")
@Tag(name = "对象存储接口", description = "文件上传、下载、删除、列举及预签名地址等操作接口")
public class OssController extends BaseController {

    /**
     * 流式拷贝缓冲区大小
     */
    private static final int COPY_BUFFER_SIZE = 8192;

    /**
     * OSS文件服务
     */
    @Resource
    private IOssFileService ossFileService;

    /**
     * 上传文件
     * <p>
     * 未指定objectKey时由服务端按存储策略自动生成；指定taskId时可通过进度查询接口轮询
     * </p>
     *
     * @param file      上传的文件
     * @param objectKey 存储对象键（可选）
     * @param bucket    存储桶（可选，缺省使用默认桶）
     * @param taskId    上传任务ID（可选，用于进度轮询）
     * @return 上传结果，包含对象键与访问地址
     */
    @PostMapping("upload")
    @AjaxResponseType(PutObjectResult.class)
    @Operation(summary = "上传文件", description = "上传文件到对象存储，可指定存储对象键与存储桶")
    @ApiResponse(responseCode = "200", description = "成功上传文件",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    @NoLog
    public AjaxResult upload(
        @Parameter(name = "file", description = "上传的文件", required = true) @RequestParam("file") MultipartFile file,
        @Parameter(name = "objectKey", description = "存储对象键，缺省自动生成") @RequestParam(value = "objectKey",
            required = false) String objectKey,
        @Parameter(name = "bucket", description = "存储桶，缺省使用默认桶") @RequestParam(value = "bucket",
            required = false) String bucket,
        @Parameter(name = "taskId", description = "上传任务ID，指定后可轮询上传进度") @RequestParam(value = "taskId",
            required = false) String taskId) throws IOException {
        OssUploadRequest request =
            OssUploadRequest.builder().inputStream(file.getInputStream()).contentLength(file.getSize())
                .contentType(file.getContentType()).originalFilename(file.getOriginalFilename()).objectKey(objectKey)
                .bucket(bucket).taskId(taskId).build();
        return successResult(ossFileService.upload(request));
    }

    /**
     * 查询上传任务进度
     * <p>
     * 供前端在上传请求执行期间轮询，任务不存在时返回RECEIVING状态
     * </p>
     *
     * @param taskId 上传任务ID
     * @return 任务进度信息
     */
    @GetMapping("upload/progress")
    @AjaxResponseType(UploadTaskProgressVO.class)
    @Operation(summary = "查询上传任务进度", description = "上传期间由前端轮询，获取服务端写入对象存储的实时进度")
    @ApiResponse(responseCode = "200", description = "成功查询上传任务进度",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    @NoLog
    public AjaxResult uploadProgress(
        @Parameter(name = "taskId", description = "上传任务ID", required = true) @RequestParam(
            "taskId") String taskId) {
        return successResult(ossFileService.getUploadProgress(taskId));
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
    @NoLog
    public void download(@Parameter(name = "objectKey", description = "存储对象键", required = true) @RequestParam(
            "objectKey") String objectKey,
        @Parameter(name = "bucket", description = "存储桶，缺省使用默认桶") @RequestParam(value = "bucket",
            required = false) String bucket, HttpServletResponse response) throws IOException {
        OssObjectStream objectStream = ossFileService.openObject(bucket, objectKey);
        applyObjectHeaders(response, objectStream);
        writeObjectStream(objectStream, response);
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
        return successResult(ossFileService.getMetadata(bucket, objectKey));
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
        return successResult(ossFileService.exists(bucket, objectKey));
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
        return successResult(ossFileService.listObjects(prefix, marker, bucket));
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
        return successResult(ossFileService.generatePresignedUrl(objectKey, type, expirationSeconds, bucket));
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
        ossFileService.deleteObject(objectKey, bucket);
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
        ossFileService.deleteObjects(bucket, objectKeys);
        return successResult();
    }

    /**
     * 组装对象下载响应头
     * <p>
     * 依据对象元数据设置内容类型与长度，文件名取对象键最后一段并做URL编码
     * </p>
     *
     * @param response     HTTP响应
     * @param objectStream 对象流（含元数据与对象键）
     */
    private void applyObjectHeaders(HttpServletResponse response, OssObjectStream objectStream) {
        ObjectMetadata metadata = objectStream.getMetadata();
        response.setContentType(
            metadata == null || StrUtil.isEmpty(metadata.getContentType()) ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : metadata.getContentType());
        if (metadata != null && metadata.getContentLength() > 0) {
            response.setContentLengthLong(metadata.getContentLength());
        }
        response.setHeader("Content-Disposition",
            "attachment;filename=" + encodeFileName(resolveFileName(objectStream.getObjectKey())));
    }

    /**
     * 输出对象内容流
     *
     * @param objectStream 对象流
     * @param response     HTTP响应
     * @throws IOException 输出流写入失败时抛出
     */
    private void writeObjectStream(OssObjectStream objectStream, HttpServletResponse response) throws IOException {
        try (InputStream inputStream = objectStream.getStream();
            OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[COPY_BUFFER_SIZE];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        }
    }

    /**
     * 解析下载展示文件名
     * <p>
     * 取对象键最后一段作为文件名
     * </p>
     *
     * @param objectKey 对象存储键
     * @return 文件名
     */
    private String resolveFileName(String objectKey) {
        return objectKey.contains("/") ? objectKey.substring(objectKey.lastIndexOf('/') + 1) : objectKey;
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
     * 获取业务名称
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "对象存储控制器";
    }
}
