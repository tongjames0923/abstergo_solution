package com.tbs.sdk.update;

import com.tbs.sdk.constants.SdkConstants;
import com.tbs.sdk.exception.SdkClientException;
import com.tbs.sdk.http.DownloadProgressListener;
import com.tbs.sdk.http.IBaseHttpClient;
import com.tbs.sdk.model.AppUpdateCheckForm;
import com.tbs.sdk.model.AppUpdateCheckResult;
import com.tbs.sdk.model.DownloadedFile;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用更新实现
 * <p>
 * 检查走公开端点（无需令牌），下载复用统一HTTP客户端连接池流式落盘
 * （临时文件原子rename防半包），多镜像URL按序容灾，全部失败抛出客户端异常。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class UpdateApi implements IUpdateApi {

    /**
     * 统一HTTP客户端
     */
    private final IBaseHttpClient httpClient;

    /**
     * 构造更新API
     *
     * @param httpClient 统一HTTP客户端
     */
    public UpdateApi(IBaseHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppUpdateCheckResult checkUpgrade(String appKey, String platform, Long currentVersionCode) {
        return checkUpgrade(SdkConstants.PATH_APP_UPDATE_CHECK, appKey, platform, currentVersionCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppUpdateCheckResult checkUpgrade(String path, String appKey, String platform, Long currentVersionCode) {
        if (appKey == null || appKey.trim().isEmpty()) {
            throw new SdkClientException("appKey不能为空");
        }
        if (platform == null || platform.trim().isEmpty()) {
            throw new SdkClientException("platform不能为空");
        }
        AppUpdateCheckForm form = new AppUpdateCheckForm();
        form.setAppKey(appKey);
        form.setPlatform(platform);
        form.setCurrentVersionCode(currentVersionCode);
        AppUpdateCheckResult result = httpClient.post(path, form, AppUpdateCheckResult.class);
        return result != null ? result : new AppUpdateCheckResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DownloadedFile> downloadAll(AppUpdateCheckResult result, File targetDir,
                                            String filePrefix, UpdateDownloadListener listener) {
        if (result == null || !Boolean.TRUE.equals(result.getNeedUpdate())) {
            throw new SdkClientException("检查结果表明无需更新，无可下载文件");
        }
        List<String> urls = result.getDownloadUrls();
        if (urls == null || urls.isEmpty()) {
            throw new SdkClientException("服务端未返回任何下载地址");
        }
        List<DownloadedFile> files = new ArrayList<>(urls.size());
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            String fileName = resolveFileName(filePrefix, url, urls.size() > 1 ? "(" + i + ")" : "");
            files.add(download(java.util.Collections.singletonList(url), targetDir, fileName, listener));
        }
        return files;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DownloadedFile download(List<String> urls, File targetDir, String fileName,
                                   UpdateDownloadListener listener) {
        if (urls == null || urls.isEmpty()) {
            throw new SdkClientException("下载地址列表不能为空");
        }
        ensureDirectory(targetDir);
        SdkClientException lastError = null;
        for (String url : urls) {
            if (url == null || url.trim().isEmpty()) {
                continue;
            }
            File target = new File(targetDir,
                fileName != null && !fileName.trim().isEmpty() ? fileName : urlFileName(url));
            try {
                return downloadSingle(url, target, listener);
            } catch (SdkClientException e) {
                lastError = e;
                if (listener != null) {
                    listener.onError(url, e);
                }
            }
        }
        throw lastError != null ? lastError : new SdkClientException("无可用的下载地址");
    }

    /**
     * 下载单个地址并回调进度
     *
     * @param url      下载地址
     * @param target   目标文件
     * @param listener 下载回调
     * @return 下载结果
     * @throws SdkClientException 下载失败时抛出
     */
    private DownloadedFile downloadSingle(String url, File target, UpdateDownloadListener listener) {
        if (listener != null) {
            listener.onStart(url);
        }
        DownloadProgressListener progress = (downloaded, total) -> {
            if (listener != null) {
                listener.onProgress(url, downloaded, total);
            }
        };
        httpClient.download(url, target, progress);
        if (listener != null) {
            listener.onComplete(target);
        }
        return DownloadedFile.of(url, target);
    }

    /**
     * 确保目标目录存在
     *
     * @param targetDir 目标目录
     * @throws SdkClientException 目录创建失败时抛出
     */
    private void ensureDirectory(File targetDir) {
        if (targetDir == null) {
            throw new SdkClientException("下载目标目录不能为空");
        }
        if (!targetDir.exists() && !targetDir.mkdirs() && !targetDir.isDirectory()) {
            throw new SdkClientException("下载目标目录创建失败: " + targetDir.getAbsolutePath());
        }
    }

    /**
     * 生成本地文件名
     * <p>
     * 有前缀时为 前缀+序号后缀，否则保留URL末段原始文件名。
     * </p>
     *
     * @param filePrefix  文件名前缀（可为null）
     * @param url         下载地址
     * @param indexSuffix 多文件时的序号后缀（单文件为空串）
     * @return 本地文件名
     */
    private String resolveFileName(String filePrefix, String url, String indexSuffix) {
        if (filePrefix != null && !filePrefix.trim().isEmpty()) {
            return filePrefix + indexSuffix;
        }
        return urlFileName(url);
    }

    /**
     * 从URL提取文件名（取路径末段，无末段时回退为下载文件）
     *
     * @param url 下载地址
     * @return 文件名
     */
    private String urlFileName(String url) {
        try {
            String path = URI.create(url).getPath();
            if (path != null && path.contains("/")) {
                String name = path.substring(path.lastIndexOf('/') + 1);
                if (!name.trim().isEmpty()) {
                    return name;
                }
            }
        } catch (Exception ignored) {
            /*
             * URL解析失败时回退到默认文件名，由下载环节继续校验地址合法性
             */
        }
        return "download";
    }
}
