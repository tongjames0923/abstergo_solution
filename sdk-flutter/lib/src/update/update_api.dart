import 'dart:io';

import '../constants/tbs_constants.dart' as c;
import '../exception/tbs_exceptions.dart';
import '../http/base_http_client.dart';
import '../model/app_update_models.dart';

/// 下载过程监听器
///
/// 回调在下载执行上下文中同步触发，耗时处理请由宿主自行转交。
abstract class UpdateDownloadListener {
  /// 单个文件开始下载
  ///
  /// @param url 下载地址
  void onStart(String url);

  /// 下载进度
  ///
  /// @param url            下载地址
  /// @param receivedBytes  已接收字节数
  /// @param totalBytes     总字节数（未知时为 -1）
  void onProgress(String url, int receivedBytes, int totalBytes);

  /// 单个文件下载完成
  ///
  /// @param url      下载地址
  /// @param filePath 落盘文件路径
  void onComplete(String url, String filePath);

  /// 下载失败（SDK 继续尝试下一镜像）
  ///
  /// @param url   下载地址
  /// @param cause 失败原因
  void onError(String url, Object cause);
}

/// 应用更新能域 API
///
/// 对齐 Java 客户端 UpdateApi：公开/管理端升级检查（同契约）+ 多镜像顺序
/// 回退下载（先写 .part 临时文件，成功后原子重命名，半包不误用）。
class UpdateApi {
  final BaseHttpClient _httpClient;

  /// 创建更新 API
  ///
  /// @param httpClient 统一 HTTP 客户端
  UpdateApi({required BaseHttpClient httpClient}) : _httpClient = httpClient;

  /// 升级检查（公开端点）
  ///
  /// @param appKey            应用标识
  /// @param platform          平台标识（AppPlatform 常量）
  /// @param currentVersionCode 客户端当前内部版本号
  /// @return 检查结果
  Future<AppUpdateCheckResult> checkUpgrade(
    String appKey,
    String platform,
    int currentVersionCode,
  ) {
    return _check(c.pathAppUpdateCheck, appKey, platform, currentVersionCode);
  }

  /// 升级检查（管理端点，需登录与查询权限）
  ///
  /// @param appKey            应用标识
  /// @param platform          平台标识
  /// @param currentVersionCode 客户端当前内部版本号
  /// @return 检查结果
  Future<AppUpdateCheckResult> checkUpgradeAdmin(
    String appKey,
    String platform,
    int currentVersionCode,
  ) {
    return _check(
      c.pathAppUpdateCheckAdmin,
      appKey,
      platform,
      currentVersionCode,
    );
  }

  /// 多镜像顺序下载（按序尝试，成功即返回，失败回调后继续下一镜像）
  ///
  /// @param urls        下载地址列表（来自检查结果 downloadUrls）
  /// @param directory   目标目录（需已存在）
  /// @param filePrefix  文件名前缀（可选，多文件时追加序号）
  /// @param listener    下载监听器（可选）
  /// @return 成功落盘的文件路径
  Future<String> download(
    List<String> urls,
    String directory,
    String? filePrefix, {
    UpdateDownloadListener? listener,
  }) async {
    if (urls.isEmpty) {
      throw const TbsClientException('下载地址列表为空');
    }
    Object? lastError;
    for (var i = 0; i < urls.length; i++) {
      final url = urls[i];
      final savePath = _resolveSavePath(directory, url, filePrefix, i);
      try {
        await _downloadOne(url, savePath, listener);
        return savePath;
      } catch (error) {
        lastError = error;
        listener?.onError(url, error);
      }
    }
    throw TbsClientException('全部镜像下载失败', cause: lastError);
  }

  /// 检查升级并按需下载最新安装包
  ///
  /// @param form              检查表单
  /// @param directory         下载目录
  /// @param filePrefix        文件名前缀（可选）
  /// @param listener          下载监听器（可选）
  /// @return 检查结果（needUpdate 且下载成功时文件路径见监听器回调）
  Future<AppUpdateCheckResult> checkAndDownload(
    AppUpdateCheckForm form,
    String directory, {
    String? filePrefix,
    UpdateDownloadListener? listener,
  }) async {
    final result = await checkUpgrade(
      form.appKey,
      form.platform,
      form.currentVersionCode,
    );
    if (result.needUpdate && result.downloadUrls.isNotEmpty) {
      await download(
        result.downloadUrls,
        directory,
        filePrefix ?? 'app-${result.latestVersionCode ?? ''}',
        listener: listener,
      );
    }
    return result;
  }

  /// 执行单次检查
  ///
  /// @param path  端点路径
  /// @param appKey 应用标识
  /// @param platform 平台标识
  /// @param currentVersionCode 当前内部版本号
  /// @return 检查结果
  Future<AppUpdateCheckResult> _check(
    String path,
    String appKey,
    String platform,
    int currentVersionCode,
  ) async {
    final response = await _httpClient.post(
      path,
      body: AppUpdateCheckForm(
        appKey: appKey,
        platform: platform,
        currentVersionCode: currentVersionCode,
      ).toJson(),
    );
    return AppUpdateCheckResult.fromJson(response.dataAsMap());
  }

  /// 下载单个地址并回调进度
  ///
  /// @param url      下载地址
  /// @param savePath 目标路径
  /// @param listener 监听器
  Future<void> _downloadOne(
    String url,
    String savePath,
    UpdateDownloadListener? listener,
  ) async {
    listener?.onStart(url);
    await _httpClient.download(
      url,
      savePath,
      onProgress: (received, total) =>
          listener?.onProgress(url, received, total),
    );
    listener?.onComplete(url, savePath);
  }

  /// 解析落盘文件路径（有前缀时追加序号，否则取 URL 末段，解析失败回退 download）
  ///
  /// @param directory  目标目录
  /// @param url        下载地址
  /// @param filePrefix 文件名前缀
  /// @param index      镜像序号
  /// @return 目标文件路径
  String _resolveSavePath(
    String directory,
    String url,
    String? filePrefix,
    int index,
  ) {
    final dir = directory.endsWith(Platform.pathSeparator)
        ? directory.substring(0, directory.length - 1)
        : directory;
    if (filePrefix != null && filePrefix.isNotEmpty) {
      final suffix = index <= 0 ? '' : '($index)';
      return '$dir${Platform.pathSeparator}$filePrefix$suffix';
    }
    final uri = Uri.tryParse(url);
    final segments = uri?.pathSegments ?? const <String>[];
    var fileName =
        segments.isNotEmpty && segments.last.isNotEmpty ? segments.last : 'download';
    if (fileName.contains('?')) {
      fileName = fileName.substring(0, fileName.indexOf('?'));
    }
    if (fileName.isEmpty) {
      fileName = 'download';
    }
    return '$dir${Platform.pathSeparator}$fileName';
  }
}
