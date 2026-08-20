package com.tbs.updater.api;

import com.tbs.updater.config.UpdaterConfig;
import com.tbs.updater.exception.UpdateException;
import com.tbs.updater.impl.Downloader;
import com.tbs.updater.impl.ScriptExecutor;
import com.tbs.updater.impl.UpdateChecker;
import com.tbs.updater.model.DownloadFile;
import com.tbs.updater.model.UpdateInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
     * 按顺序执行检查、下载全部文件、部署脚本、启动脚本：
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
            List<File> downloadedFiles = downloadAllUpdates(info);
            executeDeployScript(downloadedFiles);
            /*
             * 部署成功后立即持久化新版本号，防止启动脚本执行期间崩溃导致版本未记录
             */
            persistLocalVersion(info.getLatestVersionCode());
            /*
             * 启动脚本传入主文件（第一个）路径
             */
            File primaryFile = downloadedFiles.isEmpty() ? null : downloadedFiles.get(0);
            executeStartScript(primaryFile);
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
     * 下载所有更新文件
     * <p>
     * 遍历服务端返回的files列表，逐个下载。每个文件的多个URL按tryAllUrls配置
     * 进行镜像容灾尝试。所有文件均下载成功后返回列表。
     * </p>
     *
     * @param info 检查更新得到的更新信息
     * @return 所有下载完成的本地文件列表（顺序与files一致）
     * @throws UpdateException 任何文件下载失败时抛出DOWNLOAD_ERROR
     */
    public List<File> downloadAllUpdates(UpdateInfo info) throws UpdateException {
        if (info == null) {
            throw new UpdateException(UpdateException.ErrorType.DOWNLOAD_ERROR, "更新信息为空");
        }
        List<DownloadFile> files = info.getFiles();
        if (files == null || files.isEmpty()) {
            throw new UpdateException(UpdateException.ErrorType.DOWNLOAD_ERROR, "服务端未返回任何下载文件");
        }
        int totalFiles = files.size();
        List<File> result = new ArrayList<>(totalFiles);
        String baseName = config.getAppKey() + "-" + info.getLatestVersionCode();
        for (int fileIndex = 0; fileIndex < totalFiles; fileIndex++) {
            DownloadFile df = files.get(fileIndex);
            List<String> urls = df.getDownloadUrls();
            if (urls == null || urls.isEmpty()) {
                throw new UpdateException(UpdateException.ErrorType.DOWNLOAD_ERROR,
                        "文件[" + fileIndex + "]未返回下载地址");
            }
            /*
             * 确定本地文件名：
             * 服务端指定了name则使用name，否则用 {appKey}-{versionCode} 格式
             * 多文件时在名称后追加 ({index}) 避免冲突
             */
            String localFileName;
            if (df.getName() != null && !df.getName().trim().isEmpty()) {
                localFileName = df.getName();
            } else if (totalFiles > 1) {
                localFileName = baseName + "(" + fileIndex + ")";
            } else {
                localFileName = baseName;
            }
            int attemptLimit = config.isTryAllUrls() ? urls.size() : 1;
            UpdateException lastError = null;
            boolean downloaded = false;
            for (int i = 0; i < attemptLimit; i++) {
                String url = urls.get(i);
                listener.onDownloadFileStart(fileIndex, totalFiles, url);
                listener.onDownloadStart(url);
                try {
                    File file = downloader.download(url, config.getDownloadDir(), localFileName, listener::onDownloadProgress);
                    listener.onDownloadComplete(file);
                    result.add(file);
                    downloaded = true;
                    break;
                } catch (UpdateException e) {
                    lastError = e;
                }
            }
            if (!downloaded) {
                throw lastError != null ? lastError
                        : new UpdateException(UpdateException.ErrorType.DOWNLOAD_ERROR,
                        "文件[" + fileIndex + "]下载失败");
            }
        }
        listener.onAllDownloadsComplete(result);
        return result;
    }

    /**
     * 下载更新包（单文件兼容方法，返回第一个下载的文件）
     *
     * @param info 检查更新得到的更新信息
     * @return 下载完成的本地文件
     * @throws UpdateException 更新信息非法或所有地址均下载失败时抛出DOWNLOAD_ERROR
     * @deprecated 使用 {@link #downloadAllUpdates(UpdateInfo)} 代替
     */
    @Deprecated
    public File downloadUpdate(UpdateInfo info) throws UpdateException {
        List<File> files = downloadAllUpdates(info);
        return files.isEmpty() ? null : files.get(0);
    }

    /**
     * 执行部署脚本（多文件）
     * <p>
     * 将所有下载文件的绝对路径依次作为参数传递给脚本；
     * 脚本中 $1 为主文件，$2、$3... 为附加文件；
     * 未配置部署脚本时自动跳过本步骤。
     * </p>
     *
     * @param downloadedFiles 已下载的更新文件列表
     * @throws UpdateException 脚本执行失败或退出码非0时抛出SCRIPT_ERROR
     */
    public void executeDeployScript(List<File> downloadedFiles) throws UpdateException {
        if (isBlank(config.getDeployScript())) {
            return;
        }
        listener.onDeployStart();
        if (downloadedFiles == null || downloadedFiles.isEmpty()) {
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR,
                    "部署脚本需要文件路径，但下载文件列表为空");
        }
        String[] paths = new String[downloadedFiles.size()];
        StringBuilder pathLog = new StringBuilder();
        for (int i = 0; i < downloadedFiles.size(); i++) {
            File f = downloadedFiles.get(i);
            if (f == null || isBlank(f.getAbsolutePath())) {
                throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR,
                        "部署脚本需要文件路径，但第" + i + "个文件为空");
            }
            paths[i] = f.getAbsolutePath();
            if (i > 0) {
                pathLog.append(", ");
            }
            pathLog.append(paths[i]);
        }
        System.out.println("[deploy] 脚本: " + config.getDeployScript());
        System.out.println("[deploy] 参数(" + paths.length + "个): " + pathLog);
        int exitCode = scriptExecutor.execute(config.getDeployScript(), paths);
        if (exitCode != 0) {
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR,
                    "部署脚本执行失败, 退出码: " + exitCode);
        }
        listener.onDeployComplete();
    }

    /**
     * 执行部署脚本（单文件兼容）
     *
     * @param downloadedFile 已下载的更新包文件
     * @throws UpdateException 脚本执行失败或退出码非0时抛出SCRIPT_ERROR
     * @deprecated 使用 {@link #executeDeployScript(List)} 代替
     */
    @Deprecated
    public void executeDeployScript(File downloadedFile) throws UpdateException {
        List<File> list = new ArrayList<>(1);
        if (downloadedFile != null) {
            list.add(downloadedFile);
        }
        executeDeployScript(list);
    }

    /**
     * 执行启动脚本
     * <p>
     * 将下载文件的绝对路径作为第一个参数传递给脚本；
     * 未配置启动脚本时自动跳过本步骤。
     * </p>
     *
     * @param downloadedFile 已下载的更新包文件，可为null表示无参数启动
     * @throws UpdateException 脚本执行失败或退出码非0时抛出SCRIPT_ERROR
     */
    public void executeStartScript(File downloadedFile) throws UpdateException {
        if (isBlank(config.getStartScript())) {
            return;
        }
        listener.onStartScriptStart();
        int exitCode;
        if (downloadedFile != null && !isBlank(downloadedFile.getAbsolutePath())) {
            exitCode = scriptExecutor.execute(config.getStartScript(), downloadedFile.getAbsolutePath());
        } else {
            exitCode = scriptExecutor.execute(config.getStartScript());
        }
        if (exitCode != 0) {
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR,
                    "启动脚本执行失败, 退出码: " + exitCode);
        }
        listener.onStartScriptComplete();
    }

    /**
     * 执行启动脚本（无参数）
     *
     * @throws UpdateException 脚本执行失败或退出码非0时抛出SCRIPT_ERROR
     */
    public void executeStartScript() throws UpdateException {
        executeStartScript(null);
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

    /**
     * 将新版本号持久化到本地文件
     * <p>
     * 写入到下载目录下的 {@link UpdaterConfig#LOCAL_VERSION_FILE} 文件，
     * 下次启动时通过 {@link UpdaterConfig#readLocalVersion(String)} 读取。
     * 写入失败仅记录警告不抛异常，不影响主流程。
     * </p>
     *
     * @param newVersionCode 新安装的版本号
     */
    private void persistLocalVersion(String newVersionCode) {
        if (isBlank(newVersionCode)) {
            return;
        }
        File dir = new File(config.getDownloadDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File versionFile = new File(dir, UpdaterConfig.LOCAL_VERSION_FILE);
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(versionFile), StandardCharsets.UTF_8)) {
            writer.write(newVersionCode);
        } catch (IOException e) {
            /*
             * 版本持久化失败不影响主流程，仅通过回调通知
             */
            listener.onError("本地版本记录失败: " + e.getMessage());
        }
    }
}
