import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:tbs_client/tbs_client.dart';

import '../bootstrap/bootstrap.dart';
import '../platform/token_store.dart';

/// 环境配置 Provider
final appConfigProvider = Provider<AppConfig>((ref) => resolveAppConfig());

/// 令牌存储 Provider（平台条件实现）
final tokenStoreProvider = Provider<TokenStore>((ref) => createPlatformTokenStore());

/// TbsClient 门面 Provider（全局单例）
final tbsClientProvider = Provider<TbsClient>((ref) {
  final config = ref.watch(appConfigProvider);
  final client = TbsClient.create(
    config.baseUrl,
    config: TbsClientConfig(baseUrl: config.baseUrl),
    tokenStore: ref.watch(tokenStoreProvider),
  );
  ref.onDispose(client.dispose);
  return client;
});

/// 外观模式 Provider（持久化到本地偏好）
final themeModeProvider =
    NotifierProvider<ThemeModeController, ThemeMode>(ThemeModeController.new);

/// 语言 Provider（null 表示跟随系统；持久化到本地偏好）
final localeProvider =
    NotifierProvider<LocaleController, Locale?>(LocaleController.new);

/// 外观模式控制器
class ThemeModeController extends Notifier<ThemeMode> {
  static const String _key = 'APP_THEME_MODE';

  @override
  ThemeMode build() {
    _restore();
    return ThemeMode.system;
  }

  /// 切换外观模式并持久化
  ///
  /// @param mode 目标模式
  Future<void> setMode(ThemeMode mode) async {
    state = mode;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_key, mode.name);
  }

  Future<void> _restore() async {
    final prefs = await SharedPreferences.getInstance();
    final saved = prefs.getString(_key);
    if (saved == null) {
      return;
    }
    state = ThemeMode.values
        .where((mode) => mode.name == saved)
        .firstOrNull ?? ThemeMode.system;
  }
}

/// 语言控制器
class LocaleController extends Notifier<Locale?> {
  static const String _key = 'APP_LOCALE';

  @override
  Locale? build() {
    _restore();
    return null;
  }

  /// 切换语言并持久化（null 表示跟随系统）
  ///
  /// @param locale 目标语言
  Future<void> setLocale(Locale? locale) async {
    state = locale;
    final prefs = await SharedPreferences.getInstance();
    if (locale == null) {
      await prefs.remove(_key);
      return;
    }
    await prefs.setString(_key, locale.languageCode);
  }

  Future<void> _restore() async {
    final prefs = await SharedPreferences.getInstance();
    final saved = prefs.getString(_key);
    if (saved == null) {
      return;
    }
    state = Locale(saved);
  }
}

/// 创建平台令牌存储（宿主入口）
///
/// @return 平台令牌存储实例
TokenStore createPlatformTokenStore() => createTokenStore();
