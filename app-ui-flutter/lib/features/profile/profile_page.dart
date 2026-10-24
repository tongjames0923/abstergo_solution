import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart' show ThemeMode;
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../di/providers.dart';
import '../../di/session.dart';
import '../../features/update/update_service.dart';
import '../../shared/l10n/app_localizations.dart';
import '../../shared/widgets/app_nav_bar.dart';

/// 个人中心：用户卡、部门切换、偏好、登出与更新入口
class ProfilePage extends ConsumerWidget {
  /// 创建个人中心
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final session = ref.watch(sessionProvider);
    final user = session.value;
    return CupertinoPageScaffold(
      child: CustomScrollView(
        slivers: [
          AppNavBar(title: l10n.navProfile),
          SliverList(
            delegate: SliverChildListDelegate([
              CupertinoListSection.insetGrouped(
                children: [
                  CupertinoListTile(
                    title: Text(
                      '${user?.nickName ?? user?.userName ?? '?'}',
                      style: CupertinoTheme
                          .of(context)
                          .textTheme
                          .textStyle
                          .copyWith(fontWeight: FontWeight.w600),
                    ),
                    subtitle: user?.userName != null
                        ? Text('${user?.userName}')
                        : null,
                    leading: Container(
                      width: 56,
                      height: 56,
                      alignment: Alignment.center,
                      decoration: BoxDecoration(
                        color: CupertinoTheme
                            .of(context)
                            .primaryColor
                            .withValues(alpha: 0.12),
                        shape: BoxShape.circle,
                      ),
                      child: Text(
                        (('${user?.nickName ?? user?.userName ?? '?'}'))
                            .characters
                            .first
                            .toUpperCase(),
                        style: TextStyle(
                          fontSize: 22,
                          fontWeight: FontWeight.w700,
                          color: CupertinoTheme
                              .of(context)
                              .primaryColor,
                        ),
                      ),
                    ),
                    trailing: user?.accountStatusStr != null
                        ? Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 8,
                        vertical: 3,
                      ),
                      decoration: BoxDecoration(
                        color: CupertinoTheme
                            .of(context)
                            .primaryColor
                            .withValues(alpha: 0.10),
                        borderRadius: BorderRadius.circular(999),
                      ),
                      child: Text(
                        '${user?.accountStatusStr}',
                        style: const TextStyle(fontSize: 11),
                      ),
                    )
                        : null,
                  ),
                ],
              ),
              CupertinoListSection.insetGrouped(
                header: Text(l10n.profileDepartment),
                children: [
                  CupertinoListTile(
                    title: Text(
                      user?.currentDepartment?.resolvedName ?? '-',
                    ),
                    trailing:
                    const Icon(CupertinoIcons.chevron_forward),
                    onTap: () => _pickDepartment(context, ref, l10n),
                  ),
                ],
              ),
              CupertinoListSection.insetGrouped(
                header: Text(l10n.profileTheme),
                children: [
                  Padding(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 16,
                      vertical: 8,
                    ),
                    child: CupertinoSlidingSegmentedControl<ThemeMode>(
                      groupValue: ref.watch(themeModeProvider),
                      onValueChanged: (mode) =>
                          ref
                              .read(themeModeProvider.notifier)
                              .setMode(mode!),
                      children: {
                        ThemeMode.system: Text(l10n.profileThemeSystem),
                        ThemeMode.light: Text(l10n.profileThemeLight),
                        ThemeMode.dark: Text(l10n.profileThemeDark),
                      },
                    ),
                  ),
                ],
              ),
              CupertinoListSection.insetGrouped(
                header: Text(l10n.profileLanguage),
                children: [
                  Padding(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 16,
                      vertical: 8,
                    ),
                    child: CupertinoSlidingSegmentedControl<bool>(
                      groupValue: ref
                          .watch(localeProvider)
                          ?.languageCode != 'en',
                      onValueChanged: (isZh) =>
                          ref
                              .read(localeProvider.notifier)
                              .setLocale(
                              isZh == true ? null : const Locale('en')),
                      children: {
                        true: const Text('中文'),
                        false: const Text('English'),
                      },
                    ),
                  ),
                ],
              ),
              CupertinoListSection.insetGrouped(
                header: Text(l10n.profileVersion),
                children: [
                  CupertinoListTile(
                    title: const Text('v1.0.0'),
                    trailing: CupertinoButton(
                      padding: EdgeInsets.zero,
                      minimumSize: const Size(36, 36),
                      onPressed: () async {
                        final result = await UpdateService(
                          ref.read(tbsClientProvider),
                        ).checkAndPrompt(context);
                        if (result != null &&
                            !result.needUpdate &&
                            context.mounted) {
                          _showTip(context, l10n.profileUpToDate);
                        }
                      },
                      child: Text(l10n.profileCheckUpdate),
                    ),
                  ),
                ],
              ),
              Padding(
                padding: const EdgeInsets.all(16),
                child: CupertinoButton(
                  color: CupertinoColors.systemRed,
                  onPressed: () => _confirmLogout(context, ref, l10n),
                  child: Text(l10n.profileLogout),
                ),
              ),
            ]),
          ),
        ],
      ),
    );
  }

  /// 部门选择（CupertinoPicker 滚轮）
  Future<void> _pickDepartment(BuildContext context,
      WidgetRef ref,
      AppLocalizations l10n,) async {
    final user = ref
        .read(sessionProvider)
        .value;
    final departments =
        (user?.avaliableDepartments as List<dynamic>? ?? const [])
            .cast<dynamic>();
    if (departments.isEmpty) {
      return;
    }
    var selected = departments.indexWhere(
          (d) => '${d.resolvedId}' == '${user?.currentDepartment?.resolvedId}',
    );
    if (selected < 0) {
      selected = 0;
    }
    await showCupertinoModalPopup<void>(
      context: context,
      builder: (sheetContext) =>
          Container(
            height: 280,
            decoration: BoxDecoration(
              color: CupertinoTheme
                  .of(sheetContext)
                  .scaffoldBackgroundColor,
              borderRadius:
              const BorderRadius.vertical(top: Radius.circular(12)),
            ),
            child: SafeArea(
              top: false,
              child: Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      CupertinoButton(
                        child: Text(l10n.commonCancel),
                        onPressed: () =>
                            Navigator.of(sheetContext).pop(),
                      ),
                      CupertinoButton(
                        child: Text(l10n.commonConfirm),
                        onPressed: () {
                          final dept = departments[selected];
                          final id = '${dept.resolvedId}';
                          if (id.isNotEmpty) {
                            ref
                                .read(sessionProvider.notifier)
                                .switchDepartment(id);
                          }
                          Navigator.of(sheetContext).pop();
                        },
                      ),
                    ],
                  ),
                  Expanded(
                    child: CupertinoPicker(
                      itemExtent: 36,
                      scrollController: FixedExtentScrollController(
                        initialItem: selected,
                      ),
                      onSelectedItemChanged: (index) => selected = index,
                      children: [
                        for (final dept in departments)
                          Center(
                            child: Text('${dept.resolvedName ?? dept
                                .resolvedId}'),
                          ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
    );
  }

  /// 登出确认
  Future<void> _confirmLogout(BuildContext context,
      WidgetRef ref,
      AppLocalizations l10n,) async {
    final confirmed = await showCupertinoDialog<bool>(
      context: context,
      builder: (dialogContext) =>
          CupertinoAlertDialog(
            content: Text(l10n.profileLogoutConfirm),
            actions: [
              CupertinoDialogAction(
                onPressed: () => Navigator.of(dialogContext).pop(false),
                child: Text(l10n.commonCancel),
              ),
              CupertinoDialogAction(
                isDestructiveAction: true,
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

  /// 轻提示（无更新/更新完成等）
  void _showTip(BuildContext context, String message) {
    showCupertinoDialog<void>(
      context: context,
      builder: (dialogContext) =>
          CupertinoAlertDialog(
            content: Text(message),
            actions: [
              CupertinoDialogAction(
                onPressed: () => Navigator.of(dialogContext).pop(),
                child: Text(AppLocalizations
                    .of(context)
                    .commonConfirm),
              ),
            ],
          ),
    );
  }
}
