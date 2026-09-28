package com.tbs.sdk.http;

/**
 * 下载进度回调
 * <p>
 * 字节级进度通知，服务端未返回Content-Length时totalBytes为-1，
 * 调用方需自行判空。回调在下载线程同步执行，实现方应保持轻量。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@FunctionalInterface
public interface DownloadProgressListener {

    /**
     * 下载进度回调
     *
     * @param downloadedBytes 已下载字节数
     * @param totalBytes      总字节数，服务端未返回时为-1
     */
    void onProgress(long downloadedBytes, long totalBytes);
}
