import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:forui/forui.dart';
import 'package:tbs_client/tbs_client.dart';

import '../../di/providers.dart';
import '../../di/session.dart';
import '../../features/update/update_service.dart';
import '../../shared/l10n/app_localizations.dart';

/// 个人中心：用户卡、部门切换、偏好、登出与更新入口
class ProfilePage extends ConsumerWidget {
  /// 创建个人中心
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final session = ref.watch(sessionProvider);
    final user = session.value;
    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 640),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              _UserCard(user: user),
              const SizedBox(height: 16),
              _SectionCard(
                title: l10n.profileDepartment,
                child: _DepartmentSelector(user: user),
              ),
              const SizedBox(height: 16),
              _SectionCard(
                title: l10n.profileTheme,
                child: const _ThemeSelector(),
              ),
              const SizedBox(height: 16),
              _SectionCard(
                title: l10n.profileLanguage,
                child: const _LanguageSelector(),
              ),
              const SizedBox(height: 16),
              _SectionCard(
                title: l10n.profileVersion,
                child: const _UpdateEntry(),
              ),
              const SizedBox(height: 24),
              FButton(
                variant: .destructive,
                onPress: () => _confirmLogout(context, ref, l10n),
                child: Text(l10n.profileLogout),
              ),
            ],
          ),
        ),
      ),
    );
  }

  /// 登出确认
  Future<void> _confirmLogout(
    BuildContext context,
    WidgetRef ref,
    AppLocalizations l10n,
  ) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        content: Text(l10n.profileLogoutConfirm),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(dialogContext).pop(false),
            child: Text(l10n.commonCancel),
          ),
          FilledButton(
            onPressed: () => Navigator.of(dialogContext).pop(true),
            child: Text(l10n.commonConfirm),
          ),
        ],
      ),
    );
    if (confirmed == true) {
      await ref.read(sessionProvider.notifier).logout();
    }
  }
}

/// 用户信息卡
class _UserCard extends StatelessWidget {
  final dynamic user;

  const _UserCard({required this.user});

  @override
  Widget build(BuildContext context) {
    final name = (user?.nickName ?? user?.userName ?? '?') as String;
    final statusText = user?.accountStatusStr;
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: context.theme.colors.card,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: context.theme.colors.border),
      ),
      child: Row(
        children: [
          Container(
            width: 52,
            height: 52,
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: context.theme.colors.primary.withValues(alpha: 0.12),
              shape: BoxShape.circle,
            ),
            child: Text(
              name.characters.first.toUpperCase(),
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.w700,
                color: context.theme.colors.primary,
              ),
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  name,
                  style: context.theme.typography.body.lg
                      .copyWith(fontWeight: FontWeight.w600),
                ),
                if (user?.userName != null)
                  Text(
                    '${user.userName}',
                    style: TextStyle(
                      fontSize: 13,
                      color: context.theme.colors.mutedForeground,
                    ),
                  ),
              ],
            ),
          ),
          if (statusText != null)
            FBadge(variant: .secondary, child: Text('$statusText')),
        ],
      ),
    );
  }
}

/// 分组卡片
class _SectionCard extends StatelessWidget {
  final String title;

  final Widget child;

  const _SectionCard({required this.title, required this.child});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: context.theme.colors.card,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: context.theme.colors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: context.theme.colors.mutedForeground,
            ),
          ),
          const SizedBox(height: 12),
          child,
        ],
      ),
    );
  }
}

/// 部门切换器（FSelect 承载多部门）
class _DepartmentSelector extends ConsumerWidget {
  final dynamic user;

  const _DepartmentSelector({required this.user});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final departments =
        (user?.avaliableDepartments as List<dynamic>? ?? const [])
            .cast<dynamic>();
    if (departments.isEmpty) {
      return Text(
        '-',
        style: TextStyle(color: context.theme.colors.mutedForeground),
      );
    }
    final items = <String, String>{
      for (final dept in departments)
        dept.resolvedName ?? '${dept.resolvedId}': '${dept.resolvedId}',
    };
    return FSelect<String>(
      items: items,
      control: .managed(
        onChange: (value) {
          if (value != null && value.isNotEmpty) {
            ref.read(sessionProvider.notifier).switchDepartment(value);
          }
        },
      ),
    );
  }
}

/// 外观选择（三态切换组）
class _ThemeSelector extends ConsumerWidget {
  const _ThemeSelector();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final mode = ref.watch(themeModeProvider);
    final options = [
      (ThemeMode.system, l10n.profileThemeSystem),
      (ThemeMode.light, l10n.profileThemeLight),
      (ThemeMode.dark, l10n.profileThemeDark),
    ];
    return Row(
      children: [
        for (final option in options) ...[
          Expanded(
            child: FButton(
              variant: mode == option.$1 ? .primary : .outline,
              size: .sm,
              onPress: () =>
                  ref.read(themeModeProvider.notifier).setMode(option.$1),
              child: Text(option.$2),
            ),
          ),
          const SizedBox(width: 8),
        ],
      ],
    );
  }
}

/// 语言选择
class _LanguageSelector extends ConsumerWidget {
  const _LanguageSelector();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final locale = ref.watch(localeProvider);
    final isZh = locale?.languageCode != 'en';
    return Row(
      children: [
        Expanded(
          child: FButton(
            variant: isZh ? .primary : .outline,
            size: .sm,
            onPress: () => ref.read(localeProvider.notifier).setLocale(null),
            child: const Text('中文'),
          ),
        ),
        const SizedBox(width: 8),
        Expanded(
          child: FButton(
            variant: isZh ? .outline : .primary,
            size: .sm,
            onPress: () =>
                ref.read(localeProvider.notifier).setLocale(const Locale('en')),
            child: const Text('English'),
          ),
        ),
      ],
    );
  }
}

/// 版本与更新入口
class _UpdateEntry extends ConsumerWidget {
  const _UpdateEntry();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    return Row(
      children: [
        Expanded(
          child: Text(
            'v1.0.0',
            style: TextStyle(color: context.theme.colors.mutedForeground),
          ),
        ),
        FButton(
          variant: .outline,
          size: .sm,
          onPress: () async {
            final messenger = ScaffoldMessenger.of(context);
            final client = ref.read(tbsClientProvider);
            try {
              final result =
                  await UpdateService(client).checkAndPrompt(context);
              if (result != null && !result.needUpdate && context.mounted) {
                messenger.showSnackBar(
                  SnackBar(content: Text(l10n.profileUpToDate)),
                );
              }
            } on TbsException catch (error) {
              messenger.showSnackBar(
                SnackBar(content: Text(error.message)),
              );
            }
          },
          child: Text(l10n.profileCheckUpdate),
        ),
      ],
    );
  }
}
