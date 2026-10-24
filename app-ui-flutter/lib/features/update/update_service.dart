import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:tbs_client/tbs_client.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../bootstrap/bootstrap.dart';
import '../../shared/l10n/app_localizations.dart';

/// 应用更新服务：检查 + 三态提示 + 桌面端下载
class UpdateService {
  final TbsClient _client;

  /// 创建更新服务
  ///
  /// @param client SDK 门面
  UpdateService(this._client);

  /// 检查并弹出更新提示（无更新时返回检查结果）
  ///
  /// @param context 弹层上下文
  /// @return 检查结果
  Future<AppUpdateCheckResult?> checkAndPrompt(BuildContext context) async {
    final config = resolveAppConfig();
    final platform = _platformName();
    if (platform == null) {
      return null;
    }
    final result = await _client.update.checkUpgrade(
      config.appKey,
      platform,
      config.versionCode,
    );
    if (!context.mounted || !result.needUpdate) {
      return result;
    }
    await _showUpdateDialog(context, result);
    return result;
  }

  /// 弹出更新提示（强更不可关闭）
  Future<void> _showUpdateDialog(
    BuildContext context,
    AppUpdateCheckResult result,
  ) async {
    final l10n = AppLocalizations.of(context);
    final action = result.actionName;
    final forced = action == 'force';
    final badge = action == 'force'
        ? l10n.profileUpdateForce
        : (action == 'recommend'
            ? l10n.profileUpdateRecommend
            : l10n.profileUpdateNotify);
    await showCupertinoDialog<void>(
      context: context,
      barrierDismissible: !forced,
      builder: (dialogContext) => CupertinoAlertDialog(
        title: Text(
          l10n.profileNewVersion(
            result.latestVersionName ?? '${result.latestVersionCode ?? ''}',
          ),
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
              decoration: BoxDecoration(
                color: CupertinoTheme.of(dialogContext)
                    .primaryColor
                    .withValues(alpha: 0.10),
                borderRadius: BorderRadius.circular(999),
              ),
              child: Text(badge, style: const TextStyle(fontSize: 11)),
            ),
            const SizedBox(height: 8),
            Flexible(
              child: SingleChildScrollView(
                child: Text(result.releaseNote ?? ''),
              ),
            ),
          ],
        ),
        actions: [
          if (!forced)
            CupertinoDialogAction(
              onPressed: () => Navigator.of(dialogContext).pop(),
              child: Text(l10n.profileUpdateLater),
            ),
          CupertinoDialogAction(
            onPressed: () {
              Navigator.of(dialogContext).pop();
              _startDownload(context, result);
            },
            child: Text(l10n.profileUpdateNow),
          ),
        ],
      ),
    );
  }

  /// 执行下载：桌面端落盘（.part + 原子重命名），其余打开下载地址
  ///
  /// @param context 上下文
  /// @param result 检查结果
  Future<void> _startDownload(
    BuildContext context,
    AppUpdateCheckResult result,
  ) async {
    final l10n = AppLocalizations.of(context);
    if (kIsWeb ||
        !(Platform.isWindows || Platform.isMacOS || Platform.isLinux)) {
      final url = result.downloadUrls.firstOrNull;
      if (url != null) {
        await launchUrl(
          Uri.parse(url),
          mode: LaunchMode.externalApplication,
        );
      }
      return;
    }
    final directory = Directory.systemTemp.path;
    try {
      final file = await _client.update.download(
        result.downloadUrls,
        directory,
        'update-${result.latestVersionCode ?? ''}',
        listener: _ProgressListener(
          onProgressChanged: (received, total) {},
        ),
      );
      if (context.mounted) {
        _showTip(context, '${l10n.profileUpdateDone}: $file');
      }
    } on TbsException {
      if (context.mounted) {
        _showTip(context, l10n.profileUpdateFailed);
      }
    }
  }

  /// 轻提示
  void _showTip(BuildContext context, String message) {
    showCupertinoDialog<void>(
      context: context,
      builder: (dialogContext) => CupertinoAlertDialog(
        content: Text(message),
        actions: [
          CupertinoDialogAction(
            onPressed: () => Navigator.of(dialogContext).pop(),
            child: Text(AppLocalizations.of(context).commonConfirm),
          ),
        ],
      ),
    );
  }

  /// 当前平台标识（无对应平台时为 null）
  ///
  /// @return 平台常量
  String? _platformName() {
    if (kIsWeb) {
      return null;
    }
    if (Platform.isWindows) {
      return AppPlatform.windows;
    }
    if (Platform.isMacOS) {
      return AppPlatform.macos;
    }
    if (Platform.isLinux) {
      return AppPlatform.linux;
    }
    if (Platform.isAndroid) {
      return AppPlatform.android;
    }
    if (Platform.isIOS) {
      return AppPlatform.ios;
    }
    return null;
  }
}

/// 下载进度监听适配
class _ProgressListener extends UpdateDownloadListener {
  final void Function(int received, int total) onProgressChanged;

  /// 创建进度监听
  ///
  /// @param onProgressChanged 进度回调
  _ProgressListener({required this.onProgressChanged});

  @override
  void onStart(String url) {}

  @override
  void onProgress(String url, int receivedBytes, int totalBytes) {
    onProgressChanged(receivedBytes, totalBytes);
  }

  @override
  void onComplete(String url, String filePath) {}

  @override
  void onError(String url, Object cause) {}
}
