package com.tbs.updater.impl;

import com.tbs.updater.exception.UpdateException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 更新包下载组件
 * <p>
 * 基于HttpURLConnection流式下载远程文件到本地目录，支持HTTP重定向与进度回调。
 * 无共享可变状态，可安全复用。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class Downloader {

    /**
     * 下载进度回调接口
     *
     * @author abstergo
     * @since 1.0.0
     */
    public interface ProgressListener {

        /**
         * 进度变化回调
         *
         * @param progress 下载进度百分比（0-100）
         */
        void onProgress(int progress);

        /**
         * 详细进度回调（含字节数，用于实时计算速度与剩余时间）
         * <p>
         * 默认实现为空，需要字节级进度的调用方按需覆盖。
         * totalBytes为-1表示服务端未返回Content-Length，此时无法估算剩余时间。
         * </p>
         *
         * @param downloadedBytes 已下载字节数
         * @param totalBytes      总字节数，未知时为-1
         * @param percent         进度百分比（0-100），总量未知时仅在开始与完成时为0和100
         */
        default void onProgressDetail(long downloadedBytes, long totalBytes, int percent) {
        }
    }

    /**
     * 最大重定向次数
     */
    private static final int MAX_REDIRECTS = 5;

    /**
     * 流式读写缓冲区大小
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * 连接超时时间（毫秒）
     */
    private static final int CONNECT_TIMEOUT_MS = 30000;

    /**
     * 读取超时时间（毫秒）
     */
    private static final int READ_TIMEOUT_MS = 60000;

    /**
     * 下载文件到指定目录（自动从URL提取文件名）
     *
     * @param fileUrl          文件下载地址（可携带签名等查询参数）
     * @param saveDir          保存目录，不存在时自动创建
     * @param progressListener 进度回调，可为null表示不关心进度
     * @return 下载完成的本地文件
     * @throws UpdateException 下载过程中的IO/HTTP异常统一抛出DOWNLOAD_ERROR
     */
    public File download(String fileUrl, String saveDir, ProgressListener progressListener) throws UpdateException {
        return download(fileUrl, saveDir, null, progressListener);
    }

    /**
     * 下载文件到指定目录并指定本地文件名
     * <p>
     * 指定fileName时本地文件以该名称保存，扩展名从原始URL提取以保持文件格式一致；
     * fileName为null或空时回退为从URL提取文件名。
     * </p>
     *
     * @param fileUrl          文件下载地址（可携带签名等查询参数）
     * @param saveDir          保存目录，不存在时自动创建
     * @param fileName         期望的本地文件名（不含路径），可为null表示从URL提取
     * @param progressListener 进度回调，可为null表示不关心进度
     * @return 下载完成的本地文件
     * @throws UpdateException 下载过程中的IO/HTTP异常统一抛出DOWNLOAD_ERROR
     */
    public File download(String fileUrl, String saveDir, String fileName,
                         ProgressListener progressListener) throws UpdateException {
        HttpURLConnection connection;
        try {
            connection = openWithRedirects(fileUrl);
        } catch (IOException e) {
            throw new UpdateException(UpdateException.ErrorType.DOWNLOAD_ERROR,
                    "连接下载地址失败: " + e.getMessage(), e);
        }
        try {
            int httpStatus = connection.getResponseCode();
            if (httpStatus != HttpURLConnection.HTTP_OK) {
                throw new UpdateException(UpdateException.ErrorType.DOWNLOAD_ERROR,
                        "下载失败, HTTP状态: " + httpStatus + ", 地址: " + fileUrl);
            }
            Path dir = Paths.get(saveDir);
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new UpdateException(UpdateException.ErrorType.DOWNLOAD_ERROR,
                        "创建下载目录失败: " + saveDir, e);
            }
            File target = dir.resolve(resolveFileName(fileName, fileUrl)).toFile();
            long total = connection.getContentLengthLong();
            writeToFile(connection, target, total, progressListener);
            return target;
        } catch (IOException e) {
            throw new UpdateException(UpdateException.ErrorType.DOWNLOAD_ERROR,
                    "下载文件失败: " + e.getMessage(), e);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 打开连接并跟随重定向
     * <p>
     * 同协议重定向由HttpURLConnection自动跟随，跨协议（如http到https）
     * 通过手动解析Location逐级跳转，最多跳转{@link #MAX_REDIRECTS}次。
     * </p>
     *
     * @param fileUrl 初始下载地址
     * @return 最终可用的HTTP连接
     * @throws IOException       连接失败时抛出
     * @throws UpdateException   重定向缺少Location或超过最大次数时抛出DOWNLOAD_ERROR
     */
    private HttpURLConnection openWithRedirects(String fileUrl) throws IOException, UpdateException {
        String current = fileUrl;
        for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
            HttpURLConnection connection = (HttpURLConnection) new URL(current).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);
            int status = connection.getResponseCode();
            if (!isRedirect(status)) {
                return connection;
            }
            String location = connection.getHeaderField("Location");
            connection.disconnect();
            if (location == null || location.trim().isEmpty()) {
                throw new UpdateException(UpdateException.ErrorType.DOWNLOAD_ERROR,
                        "下载重定向缺少Location头: " + current);
            }
            current = new URL(new URL(current), location.trim()).toString();
        }
        throw new UpdateException(UpdateException.ErrorType.DOWNLOAD_ERROR,
                "下载重定向次数超限: " + fileUrl);
    }

    /**
     * 将响应流式写入目标文件并回调进度
     *
     * @param connection       HTTP连接
     * @param target           目标文件
     * @param total            文件总字节数，小于等于0表示服务端未返回Content-Length
     * @param progressListener 进度回调，可为null
     * @throws UpdateException 写入失败时抛出DOWNLOAD_ERROR
     */
    private void writeToFile(HttpURLConnection connection, File target, long total,
                             ProgressListener progressListener) throws UpdateException {
        long downloaded = 0;
        int lastProgress = 0;
        try (InputStream in = connection.getInputStream();
             OutputStream out = new FileOutputStream(target)) {
            if (progressListener != null) {
                progressListener.onProgress(0);
                progressListener.onProgressDetail(0, total > 0 ? total : -1, 0);
            }
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                downloaded += len;
                if (progressListener != null && total > 0) {
                    int progress = (int) Math.min(100, downloaded * 100 / total);
                    if (progress != lastProgress) {
                        lastProgress = progress;
                        progressListener.onProgress(progress);
                        progressListener.onProgressDetail(downloaded, total, progress);
                    }
                }
            }
            out.flush();
        } catch (IOException e) {
            /*
             * 下载中断时清理不完整文件，避免下次部署误用损坏的安装包
             */
            if (target.exists() && !target.delete()) {
                target.deleteOnExit();
            }
            throw new UpdateException(UpdateException.ErrorType.DOWNLOAD_ERROR,
                    "下载文件失败: " + e.getMessage(), e);
        }
        /*
         * 服务端未返回Content-Length时无法计算中间进度，仅在完成后上报100
         */
        if (progressListener != null) {
            progressListener.onProgress(100);
            progressListener.onProgressDetail(downloaded, total > 0 ? total : -1, 100);
        }
    }

    /**
     * 解析最终本地文件名
     * <p>
     * 指定fileName时保留其主名称部分并拼接从URL提取的原始扩展名；
     * 未指定时直接从URL提取文件名。
     * </p>
     *
     * @param fileName 调用方指定的文件名，可为null
     * @param fileUrl  下载地址（用于提取扩展名）
     * @return 最终使用的本地文件名
     */
    private String resolveFileName(String fileName, String fileUrl) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return extractFileName(fileUrl);
        }
        String extension = extractExtension(fileUrl);
        String baseName = fileName;
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        return baseName + extension;
    }

    /**
     * 从下载地址提取文件扩展名（含点号）
     *
     * @param fileUrl 下载地址
     * @return 扩展名（如 ".apk"），无法提取时返回空字符串
     */
    private String extractExtension(String fileUrl) {
        String cleaned = extractFileName(fileUrl);
        int dotIndex = cleaned.lastIndexOf('.');
        return dotIndex > 0 ? cleaned.substring(dotIndex) : "";
    }

    /**
     * 从下载地址提取文件名
     * <p>
     * 依次去掉查询参数、锚点与协议头，取路径最后一段并做URL解码，
     * 结果为空时回退为update_时间戳.tmp。
     * </p>
     *
     * @param fileUrl 下载地址
     * @return 本地保存文件名
     */
    private String extractFileName(String fileUrl) {
        String path = fileUrl;
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }
        int fragmentIndex = path.indexOf('#');
        if (fragmentIndex >= 0) {
            path = path.substring(0, fragmentIndex);
        }
        int schemeIndex = path.indexOf("://");
        if (schemeIndex >= 0) {
            path = path.substring(schemeIndex + 3);
        }
        int slashIndex = path.lastIndexOf('/');
        String name = slashIndex >= 0 ? path.substring(slashIndex + 1) : path;
        name = URLDecoder.decode(name, StandardCharsets.UTF_8);
        if (name.isEmpty()) {
            name = "update_" + System.currentTimeMillis() + ".tmp";
        }
        return name;
    }

    /**
     * 判断HTTP状态码是否为重定向
     *
     * @param status HTTP状态码
     * @return 是否为重定向状态
     */
    private boolean isRedirect(int status) {
        return status == HttpURLConnection.HTTP_MOVED_PERM
                || status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_SEE_OTHER
                || status == HttpURLConnection.HTTP_USE_PROXY
                || status == 307
                || status == 308;
    }
}
