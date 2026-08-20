package com.tbs.updater.api;

import com.tbs.updater.config.UpdaterConfig;
import com.tbs.updater.exception.UpdateException;
import com.tbs.updater.impl.Downloader;
import com.tbs.updater.impl.ScriptExecutor;
import com.tbs.updater.impl.UpdateChecker;
import com.tbs.updater.model.UpdateInfo;

import java.io.File;
import java.util.List;

/**
 * 自动更新门面类
 * <p>
 * 对外提供类库的主要API：既支持autoUpdate()一键完成
 * 检查、下载、部署、启动全流程，也支持checkForUpdate()等分步方法
 * 由调用方自行编排。全流程通过{@link UpdateListener}回调各节点状态，
 * 所有可预期错误统一抛出{@link UpdateException}。
 * 实例不持有共享可变状态，autoUpdate()为顺序执行，适合单线程调用。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class AutoUpdater {

    /**
     * 未传入监听器时使用的空实现
     */
    private static final UpdateListener NO_OP_LISTENER = new UpdateListener() {
    };

    /**
     * 更新器配置
     */
    private final UpdaterConfig config;

    /**
     * 流程回调监听器（永不为null）
     */
    private final UpdateListener listener;

    /**
     * 检查更新组件
     */
    private final UpdateChecker checker;

    /**
     * 下载组件
     */
    private final Downloader downloader;

    /**
     * 脚本执行组件
     */
    private final ScriptExecutor scriptExecutor;

    /**
     * 使用默认组件构造更新器
     *
     * @param config   更新器配置
     * @param listener 流程回调监听器，可为null表示不关心回调
     * @throws UpdateException 配置缺失或非法时抛出CONFIG_ERROR
     */
    public AutoUpdater(UpdaterConfig config, UpdateListener listener) throws UpdateException {
        this(config, listener, new UpdateChecker(), new Downloader(), new ScriptExecutor());
    }

    /**
     * 使用自定义组件构造更新器（便于测试时注入mock）
     *
     * @param config         更新器配置
     * @param listener       流程回调监听器，可为null表示不关心回调
     * @param checker        检查更新组件
     * @param downloader     下载组件
     * @param scriptExecutor 脚本执行组件
     * @throws UpdateException 配置缺失或非法时抛出CONFIG_ERROR
     */
    public AutoUpdater(UpdaterConfig config, UpdateListener listener,
                       UpdateChecker checker, Downloader downloader,
                       ScriptExecutor scriptExecutor) throws UpdateException {
        if (config == null) {
            throw new UpdateException(UpdateException.ErrorType.CONFIG_ERROR, "UpdaterConfig不能为空");
        }
        config.validate();
        this.config = config;
        this.listener = listener == null ? NO_OP_LISTENER : listener;
        this.checker = checker;
        this.downloader = downloader;
        this.scriptExecutor = scriptExecutor;
    }

    /**
     * 一键自动更新
     * <p>
     * 按顺序执行检查、下载、部署脚本、启动脚本：
     * 服务端判定无需更新时直接回调onSuccess；
     * 任一步骤失败时回调onError并终止流程。
     * 本方法不向外抛出异常，结果全部经监听器通知。
     * </p>
     */
    public void autoUpdate() {
        try {
            UpdateInfo info = checkForUpdate();
            if (!info.isNeedUpdate()) {
                listener.onSuccess();
                return;
            }
            File downloadedFile = downloadUpdate(info);
            executeDeployScript(downloadedFile);
            executeStartScript();
            listener.onSuccess();
        } catch (UpdateException e) {
            listener.onError("[" + e.getErrorType() + "] " + e.getMessage());
        } catch (Exception e) {
            listener.onError("更新流程发生未知错误: " + e.getMessage());
        }
    }

    /**
     * 检查更新
     *
     * @return 服务端返回的更新信息
     * @throws UpdateException 网络、服务端或解析错误时抛出对应类型异常
     */
    public UpdateInfo checkForUpdate() throws UpdateException {
        listener.onCheckStart();
        UpdateInfo info = checker.check(config.buildCheckUrl(), config.getAppKey(),
                config.getPlatform(), config.getCurrentVersionCode(), config.getTimeoutSeconds());
        listener.onCheckComplete(info);
        return info;
    }

    /**
     * 下载更新包
     * <p>
     * 按服务端返回的downloadUrls顺序尝试下载；
     * tryAllUrls开启时某个地址失败会继续尝试下一个，
     * 全部失败时抛出记录的最后一次异常。
     * </p>
     *
     * @param info 检查更新得到的更新信息
     * @return 下载完成的本地文件
     * @throws UpdateException 更新信息非法或所有地址均下载失败时抛出DOWNLOAD_ERROR
     */
    public File downloadUpdate(UpdateInfo info) throws UpdateException {
        if (info == null) {
            throw new UpdateException(UpdateException.ErrorType.DOWNLOAD_ERROR, "更新信息为空");
        }
        List<String> urls = info.getDownloadUrls();
        if (urls == null || urls.isEmpty()) {
            throw new UpdateException(UpdateException.ErrorType.DOWNLOAD_ERROR, "服务端未返回任何下载地址");
        }
        int attemptLimit = config.isTryAllUrls() ? urls.size() : 1;
        UpdateException lastError = null;
        for (int i = 0; i < attemptLimit; i++) {
            String url = urls.get(i);
            listener.onDownloadStart(url);
            try {
                File file = downloader.download(url, config.getDownloadDir(), listener::onDownloadProgress);
                listener.onDownloadComplete(file);
                return file;
            } catch (UpdateException e) {
                lastError = e;
            }
        }
        throw lastError;
    }

    /**
     * 执行部署脚本
     * <p>
     * 将下载文件的绝对路径作为第一个参数传递给脚本；
     * 未配置部署脚本时自动跳过本步骤。
     * </p>
     *
     * @param downloadedFile 已下载的更新包文件
     * @throws UpdateException 脚本执行失败或退出码非0时抛出SCRIPT_ERROR
     */
    public void executeDeployScript(File downloadedFile) throws UpdateException {
        if (isBlank(config.getDeployScript())) {
            return;
        }
        listener.onDeployStart();
        String packagePath = downloadedFile == null ? "" : downloadedFile.getAbsolutePath();
        int exitCode = scriptExecutor.execute(config.getDeployScript(), packagePath);
        if (exitCode != 0) {
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR,
                    "部署脚本执行失败, 退出码: " + exitCode);
        }
        listener.onDeployComplete();
    }

    /**
     * 执行启动脚本
     * <p>
     * 未配置启动脚本时自动跳过本步骤。
     * </p>
     *
     * @throws UpdateException 脚本执行失败或退出码非0时抛出SCRIPT_ERROR
     */
    public void executeStartScript() throws UpdateException {
        if (isBlank(config.getStartScript())) {
            return;
        }
        listener.onStartScriptStart();
        int exitCode = scriptExecutor.execute(config.getStartScript());
        if (exitCode != 0) {
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR,
                    "启动脚本执行失败, 退出码: " + exitCode);
        }
        listener.onStartScriptComplete();
    }

    /**
     * 获取当前配置
     *
     * @return 更新器配置
     */
    public UpdaterConfig getConfig() {
        return config;
    }

    /**
     * 判断字符串是否为空白
     *
     * @param value 待判断字符串
     * @return 为null或全空白返回true
     */
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
