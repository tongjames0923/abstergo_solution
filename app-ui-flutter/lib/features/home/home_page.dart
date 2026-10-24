
import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:tbs_client/tbs_client.dart';

import '../../di/messages.dart';
import '../../di/session.dart';
import '../../shared/design/app_theme.dart';
import '../../shared/l10n/app_localizations.dart';
import '../../shared/widgets/app_nav_bar.dart';

/// 工作台首页：问候、统计、消息预览与快捷入口
class HomePage extends ConsumerWidget {
  /// 创建首页
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final session = ref.watch(sessionProvider);
    final unread = ref.watch(unreadCountProvider);
    final messages = ref.watch(messagesProvider);
    final user = session.value;
    final hour = DateTime.now().hour;
    final greeting = hour < 12
        ? l10n.homeMorning
        : (hour < 18 ? l10n.homeAfternoon : l10n.homeEvening);
    final name = user?.nickName ?? user?.userName ?? l10n.homeNotLoggedIn;
    final dept = user?.currentDepartment?.resolvedName;
    final status = ref.watch(connectionStatusProvider);
    final online = status == ConnectionStatus.connected;
    final sessionLabel = online
        ? l10n.homeSessionOnline
        : (status == ConnectionStatus.reconnecting
            ? l10n.homeSessionReconnecting
            : l10n.homeSessionOffline);
    return CupertinoPageScaffold(
      child: CustomScrollView(
        slivers: [
          AppNavBar(title: l10n.navHome),
          SliverList(
            delegate: SliverChildListDelegate([
              CupertinoListSection.insetGrouped(
                children: [
                  CupertinoListTile(
                    title: Text('$greeting，$name'),
                    subtitle:
                        dept != null && dept.isNotEmpty ? Text(dept) : null,
                    leading: Container(
                      width: 44,
                      height: 44,
                      alignment: Alignment.center,
                      decoration: BoxDecoration(
                        color: CupertinoTheme.of(context)
                            .primaryColor
                            .withValues(alpha: 0.12),
                        shape: BoxShape.circle,
                      ),
                      child: Text(
                        name.characters.first.toUpperCase(),
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w700,
                          color: CupertinoTheme.of(context).primaryColor,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
              CupertinoListSection.insetGrouped(
                children: [
                  CupertinoListTile(
                    title: Text('$unread'),
                    subtitle: Text(l10n.homeUnreadCount),
                    leading: const Icon(
                      CupertinoIcons.envelope,
                      color: AppTheme.primary,
                    ),
                    trailing: const Icon(CupertinoIcons.chevron_forward),
                    onTap: () => context.go('/messages'),
                  ),
                  CupertinoListTile(
                    title: Text(sessionLabel),
                    subtitle: Text(l10n.homePushChannel),
                    leading: Icon(
                      CupertinoIcons.bell,
                      color: online
                          ? CupertinoColors.systemGreen
                          : CupertinoColors.systemRed,
                    ),
                  ),
                ],
              ),
              CupertinoListSection.insetGrouped(
                header: Text(l10n.homeLatestMessages),
                children: [
                  if (messages.messages.isEmpty)
                    CupertinoListTile(
                      title: Text(
                        l10n.homeNoMessages,
                        style: TextStyle(
                          color: AppTheme.secondaryLabel(context),
                        ),
                      ),
                    )
                  else
                    ...messages.messages.take(3).map(
                          (m) => CupertinoListTile(
                            title: Text(
                              m.title ?? '',
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                            subtitle: Text(
                              m.content ?? '',
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                            leading: m.readFlag
                                ? const SizedBox(width: 8)
                                : Container(
                                    width: 8,
                                    height: 8,
                                    decoration: const BoxDecoration(
                                      color: AppTheme.primary,
                                      shape: BoxShape.circle,
                                    ),
                                  ),
                          ),
                        ),
                ],
              ),
              CupertinoListSection.insetGrouped(
                header: Text(l10n.homeQuickActions),
                children: [
                  CupertinoListTile(
                    title: Text(l10n.navMessages),
                    leading: const Icon(CupertinoIcons.envelope),
                    trailing: const Icon(CupertinoIcons.chevron_forward),
                    onTap: () => context.go('/messages'),
                  ),
                  CupertinoListTile(
                    title: Text(l10n.navProfile),
                    leading: const Icon(CupertinoIcons.person),
                    trailing: const Icon(CupertinoIcons.chevron_forward),
                    onTap: () => context.go('/profile'),
                  ),
                  CupertinoListTile(
                    title: Text(l10n.homeCheckUpdate),
                    leading: const Icon(CupertinoIcons.arrow_clockwise),
                    trailing: const Icon(CupertinoIcons.chevron_forward),
                    onTap: () => context.go('/profile'),
                  ),
                ],
              ),
            ]),
          ),
        ],
      ),
    );
  }
}
