import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:forui/forui.dart';
import 'package:tbs_client/tbs_client.dart';

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
    return MaterialApp.router(
      title: 'TBS Workspace',
      debugShowCheckedModeBanner: false,
      themeMode: themeMode,
      theme: AppTheme.materialData(Brightness.light),
      darkTheme: AppTheme.materialData(Brightness.dark),
      locale: locale,
      localizationsDelegates: const [
        AppLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        ...FLocalizations.localizationsDelegates,
      ],
      supportedLocales: const [
        Locale('zh'),
        Locale('en'),
        ...FLocalizations.supportedLocales,
      ],
      routerConfig: router,
      builder: (context, child) => FTheme(
        data: AppTheme.foruiData(
          Theme.of(context).brightness,
          touch: MediaQuery.sizeOf(context).width < 720,
        ),
        child: FToaster(child: FTooltipGroup(child: child!)),
      ),
    );
  }

  /// 订阅 SDK 全局事件（会话失效引导重登）
  void _listenClientEvents(WidgetRef ref) {
    final client = ref.read(tbsClientProvider);
    _bindOnce(client, ref);
  }

  /// 事件流一次性绑定（Provider 重建时防重复）
  void _bindOnce(TbsClient client, WidgetRef ref) {
    client.onSessionExpired.listen((event) {
      ref.read(sessionProvider.notifier).logout();
    });
  }
}
