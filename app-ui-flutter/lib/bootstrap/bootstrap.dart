import 'dart:developer' as developer;

import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';
import 'desktop_window.dart';

/// 应用运行环境配置
class AppConfig {
  /// 服务端基础地址
  final String baseUrl;

  /// 应用标识（升级检查用）
  final String appKey;

  /// 客户端内部版本号
  final int versionCode;

  /// 创建环境配置
  const AppConfig({
    required this.baseUrl,
    required this.appKey,
    required this.versionCode,
  });
}

/// 启动环境配置（baseUrl/应用标识走 --dart-define 注入）
///
/// @return 环境配置
AppConfig resolveAppConfig() {
  const baseUrl = String.fromEnvironment(
    'TBS_BASE_URL',
    defaultValue: 'http://192.168.80.32:8082',
  );
  const appKey = String.fromEnvironment('TBS_APP_KEY', defaultValue: 'tbs-client');
  const versionCode = int.fromEnvironment('TBS_APP_VERSION_CODE', defaultValue: 1);
  return AppConfig(baseUrl: baseUrl, appKey: appKey, versionCode: versionCode);
}

/// 启动编排：日志装配 + 桌面窗口初始化
///
/// @param title 窗口标题
Future<void> bootstrap(String title) async {
  _setupLogging();
  await DesktopWindow.init(title);
}

/// 装配日志分级（仅调试模式输出，生产静默；令牌/凭证不落日志）
void _setupLogging() {
  if (!kDebugMode) {
    return;
  }
  Logger.root.level = Level.INFO;
  Logger.root.onRecord.listen((record) {
    developer.log(
      record.message,
      name: record.loggerName,
      level: record.level.value,
      error: record.error,
    );
  });
}
