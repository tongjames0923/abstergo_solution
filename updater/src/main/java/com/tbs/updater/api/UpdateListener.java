package com.tbs.updater.api;

import com.tbs.updater.model.UpdateInfo;

import java.io.File;
import java.util.List;

/**
 * 更新流程回调监听接口
 * <p>
 * 所有方法均为default空实现，调用方按需覆盖关心的节点。
 * 回调在更新流程执行的线程上同步触发，实现方不应在回调中执行耗时操作。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface UpdateListener {

    /**
     * 检查更新开始
     */
    default void onCheckStart() {
    }

    /**
     * 检查更新完成
     *
     * @param info 服务端返回的更新信息
     */
    default void onCheckComplete(UpdateInfo info) {
    }

    /**
     * 开始下载某个文件（多文件场景）
     *
     * @param fileIndex  当前文件索引（从0开始）
     * @param totalFiles 文件总数
     * @param url        当前尝试的下载地址
     */
    default void onDownloadFileStart(int fileIndex, int totalFiles, String url) {
    }

    /**
     * 开始从指定地址下载更新包
     *
     * @param url 当前尝试的下载地址
     */
    default void onDownloadStart(String url) {
    }

    /**
     * 下载进度变化
     *
     * @param progress 下载进度百分比（0-100）
     */
    default void onDownloadProgress(int progress) {
    }

    /**
     * 下载进度变化（含字节数，用于实时计算速度与剩余时间）
     * <p>
     * totalBytes为-1表示服务端未返回Content-Length，此时无法估算剩余时间。
     * </p>
     *
     * @param downloadedBytes 已下载字节数
     * @param totalBytes      总字节数，未知时为-1
     * @param percent         进度百分比（0-100）
     */
    default void onDownloadProgressDetail(long downloadedBytes, long totalBytes, int percent) {
    }

    /**
     * 下载完成
     *
     * @param downloadedFile 下载完成的本地文件
     */
    default void onDownloadComplete(File downloadedFile) {
    }

    /**
     * 所有文件下载完成
     *
     * @param downloadedFiles 所有下载完成的本地文件列表
     */
    default void onAllDownloadsComplete(List<File> downloadedFiles) {
    }

    /**
     * 部署脚本开始执行（脚本即将后台启动，不等待其结束）
     */
    default void onDeployStart() {
    }

    /**
     * 部署脚本已启动（后台运行，不代表脚本已执行完毕）
     */
    default void onDeployComplete() {
    }

    /**
     * 整个更新流程成功（含"已是最新版本"的直接成功）
     */
    default void onSuccess() {
    }

    /**
     * 整个更新流程失败
     *
     * @param errMsg 错误描述
     */
    default void onError(String errMsg) {
    }
}
