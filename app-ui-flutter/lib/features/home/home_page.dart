import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:forui/forui.dart';
import 'package:go_router/go_router.dart';
import 'package:tbs_client/tbs_client.dart';

import '../../di/messages.dart';
import '../../di/session.dart';
import '../../shared/l10n/app_localizations.dart';

/// 工作台首页：问候、统计卡、消息预览与快捷入口
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
    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 960),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _Greeting(user: user, l10n: l10n),
              const SizedBox(height: 20),
              Row(
                children: [
                  Expanded(
                    child: _StatCard(
                      icon: Icons.mark_email_unread_rounded,
                      label: l10n.homeUnreadCount,
                      value: '$unread',
                    ),
                  ),
                  const SizedBox(width: 12),
                  const Expanded(child: _SessionCard()),
                ],
              ),
              const SizedBox(height: 20),
              Text(
                l10n.homeLatestMessages,
                style: context.theme.typography.body.lg
                    .copyWith(fontWeight: FontWeight.w600),
              ),
              const SizedBox(height: 10),
              if (messages.messages.isEmpty)
                _EmptyHint(text: l10n.homeNoMessages)
              else
                ...messages.messages.take(3).map(
                      (m) => _MessagePreview(message: m),
                    ),
              const SizedBox(height: 20),
              Text(
                l10n.homeQuickActions,
                style: context.theme.typography.body.lg
                    .copyWith(fontWeight: FontWeight.w600),
              ),
              const SizedBox(height: 10),
              _QuickActions(l10n: l10n),
            ],
          ),
        ),
      ),
    );
  }
}

/// 问候头部
class _Greeting extends StatelessWidget {
  final UserInfoModel? user;

  final AppLocalizations l10n;

  const _Greeting({required this.user, required this.l10n});

  @override
  Widget build(BuildContext context) {
    final hour = DateTime.now().hour;
    final greeting = hour < 12
        ? l10n.homeMorning
        : (hour < 18 ? l10n.homeAfternoon : l10n.homeEvening);
    final name = user?.nickName ?? user?.userName ?? l10n.homeNotLoggedIn;
    final dept = user?.currentDepartment?.resolvedName;
    return Row(
      children: [
        Container(
          width: 48,
          height: 48,
          alignment: Alignment.center,
          decoration: BoxDecoration(
            color: context.theme.colors.primary.withValues(alpha: 0.12),
            borderRadius: BorderRadius.circular(14),
          ),
          child: Text(
            name.characters.first.toUpperCase(),
            style: TextStyle(
              fontSize: 20,
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
                '$greeting，$name',
                style: context.theme.typography.body.xl
                    .copyWith(fontWeight: FontWeight.w700),
              ),
              if (dept != null && dept.isNotEmpty)
                Text(
                  dept,
                  style: TextStyle(
                    fontSize: 13,
                    color: context.theme.colors.mutedForeground,
                  ),
                ),
            ],
          ),
        ),
      ],
    );
  }
}

/// 指标卡
class _StatCard extends StatelessWidget {
  final IconData icon;

  final String label;

  final String value;

  const _StatCard({
    required this.icon,
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: context.theme.colors.card,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: context.theme.colors.border),
      ),
      child: Row(
        children: [
          Container(
            width: 38,
            height: 38,
            decoration: BoxDecoration(
              color: context.theme.colors.secondary,
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, size: 20, color: context.theme.colors.primary),
          ),
          const SizedBox(width: 12),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                value,
                style: context.theme.typography.body.lg
                    .copyWith(fontWeight: FontWeight.w700),
              ),
              Text(
                label,
                style: TextStyle(
                  fontSize: 12,
                  color: context.theme.colors.mutedForeground,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

/// 会话状态卡
class _SessionCard extends ConsumerWidget {
  const _SessionCard();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final status = ref.watch(connectionStatusProvider);
    final online = status == ConnectionStatus.connected;
    final label = online
        ? l10n.homeSessionOnline
        : (status == ConnectionStatus.reconnecting
            ? l10n.homeSessionReconnecting
            : l10n.homeSessionOffline);
    final color = online ? const Color(0xFF16A34A) : const Color(0xFFDC2626);
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: context.theme.colors.card,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: context.theme.colors.border),
      ),
      child: Row(
        children: [
          Container(
            width: 38,
            height: 38,
            decoration: BoxDecoration(
              color: color.withValues(alpha: 0.12),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(Icons.sensors_rounded, size: 20, color: color),
          ),
          const SizedBox(width: 12),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: context.theme.typography.body.lg
                    .copyWith(fontWeight: FontWeight.w700),
              ),
              Text(
                l10n.homePushChannel,
                style: TextStyle(
                  fontSize: 12,
                  color: context.theme.colors.mutedForeground,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

/// 消息预览条目
class _MessagePreview extends StatelessWidget {
  final PushNotifyMessage message;

  const _MessagePreview({required this.message});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: context.theme.colors.card,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: context.theme.colors.border),
      ),
      child: Row(
        children: [
          if (!message.readFlag)
            Container(
              width: 8,
              height: 8,
              margin: const EdgeInsets.only(right: 10),
              decoration: BoxDecoration(
                color: context.theme.colors.primary,
                shape: BoxShape.circle,
              ),
            )
          else
            const SizedBox(width: 18),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  message.title ?? '',
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontWeight: FontWeight.w600),
                ),
                const SizedBox(height: 2),
                Text(
                  message.content ?? '',
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    fontSize: 12,
                    color: context.theme.colors.mutedForeground,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// 快捷入口宫格
class _QuickActions extends StatelessWidget {
  final AppLocalizations l10n;

  const _QuickActions({required this.l10n});

  @override
  Widget build(BuildContext context) {
    final actions = [
      (Icons.inbox_rounded, l10n.navMessages, '/messages'),
      (Icons.person_rounded, l10n.navProfile, '/profile'),
      (Icons.upgrade_rounded, l10n.homeCheckUpdate, '/profile'),
    ];
    return Row(
      children: [
        for (final action in actions)
          Expanded(
            child: Padding(
              padding: const EdgeInsets.only(right: 12),
              child: Material(
                color: context.theme.colors.card,
                borderRadius: BorderRadius.circular(14),
                child: InkWell(
                  borderRadius: BorderRadius.circular(14),
                  onTap: () => context.go(action.$3),
                  child: Container(
                    padding: const EdgeInsets.symmetric(vertical: 18),
                    child: Column(
                      children: [
                        Icon(
                          action.$1,
                          size: 24,
                          color: context.theme.colors.primary,
                        ),
                        const SizedBox(height: 8),
                        Text(action.$2, style: const TextStyle(fontSize: 13)),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
      ],
    );
  }
}

/// 空态提示
class _EmptyHint extends StatelessWidget {
  final String text;

  const _EmptyHint({required this.text});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: context.theme.colors.card,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: context.theme.colors.border),
      ),
      child: Text(
        text,
        style: TextStyle(color: context.theme.colors.mutedForeground),
      ),
    );
  }
}
