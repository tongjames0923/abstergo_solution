import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:forui/forui.dart';
import 'package:tbs_client/tbs_client.dart';

import '../di/providers.dart';
import '../di/session.dart';
import '../shared/l10n/app_localizations.dart';

/// 顶栏：SSE 连接状态、语言与外观切换
class AppTopBar extends ConsumerWidget {
  /// 创建顶栏
  ///
  /// @param title 当前页标题
  const AppTopBar({super.key, required this.title});

  /// 当前页标题
  final String title;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final status = ref.watch(connectionStatusProvider);
    return Container(
      height: 56,
      padding: const EdgeInsets.symmetric(horizontal: 16),
      decoration: BoxDecoration(
        color: context.theme.colors.background,
        border: Border(bottom: BorderSide(color: context.theme.colors.border)),
      ),
      child: Row(
        children: [
          Text(title, style: context.theme.typography.body.xl),
          const Spacer(),
          _ConnectionBadge(status: status, l10n: l10n),
          const SizedBox(width: 8),
          _LocaleToggle(l10n: l10n, ref: ref),
          const SizedBox(width: 4),
          _ThemeToggle(ref: ref),
        ],
      ),
    );
  }
}

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
        ? const Color(0xFF16A34A)
        : (connecting ? const Color(0xFFD97706) : const Color(0xFFDC2626));
    final label = connected
        ? l10n.homeSessionOnline
        : (status == ConnectionStatus.reconnecting
            ? l10n.homeSessionReconnecting
            : (connecting ? l10n.homeSessionConnecting : l10n.homeSessionOffline));
    return Tooltip(
      message: label,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 8,
            height: 8,
            decoration: BoxDecoration(color: color, shape: BoxShape.circle),
          ),
          const SizedBox(width: 6),
          Text(
            label,
            style: TextStyle(
              fontSize: 12,
              color: context.theme.colors.mutedForeground,
            ),
          ),
        ],
      ),
    );
  }
}

class _LocaleToggle extends ConsumerWidget {
  final AppLocalizations l10n;

  final WidgetRef ref;

  const _LocaleToggle({required this.l10n, required this.ref});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final locale = ref.watch(localeProvider);
    final isZh = locale?.languageCode != 'en';
    return IconButton(
      tooltip: l10n.profileLanguage,
      onPressed: () =>
          ref.read(localeProvider.notifier).setLocale(isZh ? const Locale('en') : null),
      icon: Icon(
        isZh ? Icons.translate_rounded : Icons.g_translate_rounded,
        size: 20,
        color: context.theme.colors.mutedForeground,
      ),
    );
  }
}

class _ThemeToggle extends ConsumerWidget {
  final WidgetRef ref;

  const _ThemeToggle({required this.ref});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final mode = ref.watch(themeModeProvider);
    final dark = mode == ThemeMode.dark ||
        (mode == ThemeMode.system &&
            MediaQuery.platformBrightnessOf(context) == Brightness.dark);
    return IconButton(
      tooltip: context.theme.colors.background.toString(),
      onPressed: () => ref.read(themeModeProvider.notifier).setMode(
            dark ? ThemeMode.light : ThemeMode.dark,
          ),
      icon: Icon(
        dark ? Icons.light_mode_rounded : Icons.dark_mode_rounded,
        size: 20,
        color: context.theme.colors.mutedForeground,
      ),
    );
  }
}
