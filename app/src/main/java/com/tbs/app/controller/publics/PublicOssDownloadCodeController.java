package com.tbs.app.controller.publics;

import com.tbs.app.service.oss.IOssFileService;
import com.tbs.app.service.oss.OssObjectStream;
import com.tbs.app.service.oss.downloadcode.IOssDownloadCodeService;
import com.tbs.common.util.base.StrUtil;
import com.tbs.logging.annotation.NoLog;
import com.tbs.oss.downloadcode.OssDownloadCodeInfo;
import com.tbs.oss.model.ObjectMetadata;
import com.tbs.web.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OSS公开下载码下载控制器
 * <p>
 * 面向系统集成方的免认证下载入口：凭下载唯一码直接下载对应的OSS对象。
 * 位于 {@code /public/**} 路径下无需认证，码无效或已过期时返回用户级错误且不泄露存储细节；
 * 仅做HTTP接入与响应组装，对象内容经 {@link IOssFileService} 获取，不直接依赖对象存储门面
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/public/oss/download-code")
@Tag(name = "OSS公开下载码下载接口", description = "凭下载唯一码免认证下载文件，面向系统集成场景")
public class PublicOssDownloadCodeController extends BaseController {

    /**
     * 流式拷贝缓冲区大小
     */
    private static final int COPY_BUFFER_SIZE = 8192;

    /**
     * 下载码服务
     */
    @Resource
    private IOssDownloadCodeService ossDownloadCodeService;

    /**
     * OSS文件服务
     */
    @Resource
    private IOssFileService ossFileService;

    /**
     * 凭下载唯一码下载文件
     * <p>
     * 校验唯一码有效性与过期状态后，以附件形式流式输出对象内容；
     * 文件名取对象键最后一段并做URL编码
     * </p>
     *
     * @param code     下载唯一码
     * @param response HTTP响应
     * @throws IOException 输出流写入失败时抛出
     */
    @GetMapping("download")
    @Operation(summary = "凭码下载文件", description = "凭下载唯一码免认证下载对应的存储对象，以附件形式输出")
    @ApiResponse(responseCode = "200", description = "成功下载文件")
    @NoLog
    public void download(@Parameter(name = "code", description = "下载唯一码",
        required = true) @RequestParam("code") String code, HttpServletResponse response) throws IOException {
        OssDownloadCodeInfo info = ossDownloadCodeService.resolveForDownload(code);
        OssObjectStream objectStream = ossFileService.openObject(info.getBucket(), info.getObjectKey());
        applyObjectHeaders(response, objectStream);
        writeObjectStream(objectStream, response);
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
        return "OSS公开下载码下载";
    }
}
