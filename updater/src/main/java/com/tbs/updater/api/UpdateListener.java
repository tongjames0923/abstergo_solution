package com.tbs.updater.api;

import com.tbs.updater.model.UpdateInfo;

import java.io.File;

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
     * 下载完成
     *
     * @param downloadedFile 下载完成的本地文件
     */
    default void onDownloadComplete(File downloadedFile) {
    }

    /**
     * 部署脚本开始执行
     */
    default void onDeployStart() {
    }

    /**
     * 部署脚本执行完成
     */
    default void onDeployComplete() {
    }

    /**
     * 启动脚本开始执行
     */
    default void onStartScriptStart() {
    }

    /**
     * 启动脚本执行完成
     */
    default void onStartScriptComplete() {
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
