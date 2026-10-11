package com.tbs.sdk.update;

import com.tbs.sdk.model.AppUpdateCheckResult;
import com.tbs.sdk.model.DownloadedFile;
import java.io.File;
import java.util.List;

/**
 * 应用更新契约
 * <p>
 * 封装"请求更新（升级检查）+ 下载更新"两个能力：检查为公开接口无需登录，
 * 下载对服务端返回的OSS预签名/外部托管地址流式落盘。
 * 部署脚本执行与本地版本持久化不在SDK职责内（由updater模块承担）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IUpdateApi {

    /**
     * 升级检查（请求更新）
     * <p>
     * 客户端上报应用标识、平台与当前内部版本号，服务端综合最新已发布版本
     * 与生效的兼容策略返回是否需要升级及下载地址。公开接口，无需登录令牌。
     * </p>
     *
     * @param appKey             应用标识（必填）
     * @param platform           平台标识，如ANDROID/IOS/WEB（必填）
     * @param currentVersionCode 客户端当前内部版本号
     * @return 升级检查结果（needUpdate为false时其余字段为空）
     * @throws com.tbs.sdk.exception.SdkServerException 服务端错误时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络失败或响应非法时抛出
     */
    AppUpdateCheckResult checkUpgrade(String appKey, String platform, Long currentVersionCode);

    /**
     * 升级检查（指定端点）
     * <p>
     * 公开端点与 {@link com.tbs.sdk.constants.SdkConstants#PATH_APP_UPDATE_CHECK_ADMIN 管理端检查端点}
     * 同契约，管理端需登录令牌与查询权限。{@link #checkUpgrade(String, String, Long)} 为本方法走公开端点的便捷重载。
     * </p>
     *
     * @param path               检查端点路径（公开或管理端）
     * @param appKey             应用标识（必填）
     * @param platform           平台标识（必填，见 {@link com.tbs.sdk.model.AppPlatform}）
     * @param currentVersionCode 客户端当前内部版本号
     * @return 升级检查结果（needUpdate为false时其余字段为空）
     * @throws com.tbs.sdk.exception.SdkServerException 服务端错误时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 参数非法、网络失败或响应非法时抛出
     */
    AppUpdateCheckResult checkUpgrade(String path, String appKey, String platform, Long currentVersionCode);

    /**
     * 下载全部更新包
     * <p>
     * 按downloadUrls顺序逐个下载（每个URL一个目标文件），全部成功后返回列表；
     * 任一文件所有镜像地址均失败时抛出异常并终止。
     * </p>
     *
     * @param result      升级检查结果（needUpdate必须为true且downloadUrls非空）
     * @param targetDir   下载目标目录（不存在时自动创建）
     * @param filePrefix  本地文件名前缀（建议appKey-版本号，可为null取URL文件名）
     * @param listener    下载回调（可为null）
     * @return 下载完成的文件列表（顺序与downloadUrls一致）
     * @throws com.tbs.sdk.exception.SdkClientException 检查结果非法或全部镜像下载失败时抛出
     */
    List<DownloadedFile> downloadAll(AppUpdateCheckResult result, File targetDir,
                                     String filePrefix, UpdateDownloadListener listener);

    /**
     * 下载单个文件
     * <p>
     * 多镜像按序容灾：任一地址成功即返回；全部失败抛出异常。
     * </p>
     *
     * @param urls      候选下载地址列表（至少一个）
     * @param targetDir 下载目标目录（不存在时自动创建）
     * @param fileName  本地文件名（为null时取URL末段）
     * @param listener  下载回调（可为null）
     * @return 下载结果（源地址+本地文件）
     * @throws com.tbs.sdk.exception.SdkClientException 地址非法或全部镜像下载失败时抛出
     */
    DownloadedFile download(List<String> urls, File targetDir, String fileName,
                            UpdateDownloadListener listener);
}
