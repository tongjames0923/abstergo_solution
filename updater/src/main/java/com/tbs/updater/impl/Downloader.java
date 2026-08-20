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
     * 下载文件到指定目录
     * <p>
     * 文件名取自URL路径最后一段（去掉查询参数并做URL解码），无法提取时使用
     * update_时间戳.tmp。下载失败时清理不完整的临时文件。
     * </p>
     *
     * @param fileUrl          文件下载地址（可携带签名等查询参数）
     * @param saveDir          保存目录，不存在时自动创建
     * @param progressListener 进度回调，可为null表示不关心进度
     * @return 下载完成的本地文件
     * @throws UpdateException 下载过程中的IO/HTTP异常统一抛出DOWNLOAD_ERROR
     */
    public File download(String fileUrl, String saveDir, ProgressListener progressListener) throws UpdateException {
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
            File target = dir.resolve(extractFileName(fileUrl)).toFile();
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
        }
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
