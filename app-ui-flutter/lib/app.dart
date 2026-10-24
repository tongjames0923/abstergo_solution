import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart' show ThemeMode;
import 'package:flutter_localizations/flutter_localizations.dart'
;
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'di/providers.dart';
import 'di/session.dart';
import 'routing/app_router.dart';
import 'shared/design/app_theme.dart';
import 'shared/l10n/app_localizations.dart';

/// 应用根组件：主题/语言装配 + 全局事件监听 + 路由挂载
class TbsApp extends ConsumerWidget {
  /// 创建应用根组件
  const TbsApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final themeMode = ref.watch(themeModeProvider);
    final locale = ref.watch(localeProvider);
    final router = ref.watch(appRouterProvider);
    _listenClientEvents(ref);
    final dark = themeMode == ThemeMode.dark ||
        (themeMode == ThemeMode.system &&
            WidgetsBinding.instance.platformDispatcher.platformBrightness ==
                Brightness.dark);
    return CupertinoApp.router(
      title: 'TBS Workspace',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.data(dark ? Brightness.dark : Brightness.light),
      locale: locale,
      localizationsDelegates: const [
        AppLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      supportedLocales: const [
        Locale('zh'),
        Locale('en'),
      ],
      routerConfig: router,
    );
  }

  /// 订阅 SDK 全局事件（会话失效引导重登）
  void _listenClientEvents(WidgetRef ref) {
    final client = ref.read(tbsClientProvider);
    client.onSessionExpired.listen((event) {
      ref.read(sessionProvider.notifier).logout();
    });
  }
}
