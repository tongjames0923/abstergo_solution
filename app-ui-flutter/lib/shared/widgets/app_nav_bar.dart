import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart' show ThemeMode;
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:tbs_client/tbs_client.dart';

import '../../di/providers.dart';
import '../../di/session.dart';
import '../design/app_theme.dart';
import '../l10n/app_localizations.dart';

/// 应用统一导航栏：Large Title + 连接状态 + 语言/外观操作
class AppNavBar extends ConsumerWidget {
  /// 页面标题
  final String title;

  /// 创建导航栏
  ///
  /// @param title 页面标题
  const AppNavBar({super.key, required this.title});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final status = ref.watch(connectionStatusProvider);
    final locale = ref.watch(localeProvider);
    final isZh = locale?.languageCode != 'en';
    final mode = ref.watch(themeModeProvider);
    final dark = mode == ThemeMode.dark ||
        (mode == ThemeMode.system &&
            MediaQuery.platformBrightnessOf(context) == Brightness.dark);
    final theme = CupertinoTheme.of(context);
    return CupertinoSliverNavigationBar(
      largeTitle: Text(title),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          _ConnectionBadge(status: status, l10n: l10n),
          const SizedBox(width: 4),
          CupertinoButton(
            padding: EdgeInsets.zero,
            minimumSize: const Size(36, 36),
            onPressed: () => ref
                .read(localeProvider.notifier)
                .setLocale(isZh ? const Locale('en') : null),
            child: Icon(
              CupertinoIcons.globe,
              size: 21,
              color: theme.primaryColor,
            ),
          ),
          CupertinoButton(
            padding: EdgeInsets.zero,
            minimumSize: const Size(36, 36),
            onPressed: () => ref
                .read(themeModeProvider.notifier)
                .setMode(dark ? ThemeMode.light : ThemeMode.dark),
            child: Icon(
              dark ? CupertinoIcons.sun_max : CupertinoIcons.moon,
              size: 21,
              color: theme.primaryColor,
            ),
          ),
        ],
      ),
    );
  }
}

/// 连接状态徽标
class _ConnectionBadge extends StatelessWidget {
  final ConnectionStatus status;

  final AppLocalizations l10n;

  const _ConnectionBadge({required this.status, required this.l10n});

  @override
  Widget build(BuildContext context) {
    final connected = status == ConnectionStatus.connected;
    final connecting = status == ConnectionStatus.connecting ||
        status == ConnectionStatus.reconnecting;
    final color = connected
        ? CupertinoColors.systemGreen
        : (connecting ? CupertinoColors.systemOrange : CupertinoColors.systemRed);
    final label = connected
        ? l10n.homeSessionOnline
        : (status == ConnectionStatus.reconnecting
            ? l10n.homeSessionReconnecting
            : (connecting
                ? l10n.homeSessionConnecting
                : l10n.homeSessionOffline));
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 7,
          height: 7,
          decoration: BoxDecoration(color: color, shape: BoxShape.circle),
        ),
        const SizedBox(width: 5),
        Text(
          label,
          style: TextStyle(
            fontSize: 11,
            color: CupertinoDynamicColor.resolve(color, context),
          ),
        ),
      ],
    );
  }
}
