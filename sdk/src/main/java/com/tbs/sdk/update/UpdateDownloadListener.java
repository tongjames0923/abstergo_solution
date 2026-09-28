package com.tbs.sdk.update;

import java.io.File;

/**
 * 更新下载回调监听器
 * <p>
 * 全部方法均为默认空实现，调用方按需覆写。回调在下载线程同步执行，
 * 实现方应保持轻量（如仅更新UI进度），耗时处理自行转交业务线程池。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface UpdateDownloadListener {

    /**
     * 单个下载任务开始
     *
     * @param url 下载地址
     */
    default void onStart(String url) {
    }

    /**
     * 下载进度回调
     *
     * @param url             下载地址
     * @param downloadedBytes 已下载字节数
     * @param totalBytes      总字节数，服务端未返回Content-Length时为-1
     */
    default void onProgress(String url, long downloadedBytes, long totalBytes) {
    }

    /**
     * 单个文件下载完成
     *
     * @param file 本地落盘文件
     */
    default void onComplete(File file) {
    }

    /**
     * 单个下载地址失败（多镜像场景下SDK会继续尝试下一地址，本回调仅作通知）
     *
     * @param url   失败的下载地址
     * @param cause 失败原因
     */
    default void onError(String url, Throwable cause) {
    }
}
