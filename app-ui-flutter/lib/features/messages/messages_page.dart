import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:forui/forui.dart';
import 'package:tbs_client/tbs_client.dart';

import '../../di/providers.dart';

import '../../di/messages.dart';
import '../../di/session.dart';
import '../../shared/l10n/app_localizations.dart';

/// 消息中心：筛选、游标无限滚动、批量已读与详情
class MessagesPage extends ConsumerStatefulWidget {
  /// 创建消息中心
  const MessagesPage({super.key});

  @override
  ConsumerState<MessagesPage> createState() => _MessagesPageState();
}

class _MessagesPageState extends ConsumerState<MessagesPage> {
  final ScrollController _scroll = ScrollController();

  @override
  void initState() {
    super.initState();
    _scroll.addListener(_onScroll);
    Future.microtask(
      () => ref.read(unreadCountProvider.notifier).refresh(),
    );
  }

  @override
  void dispose() {
    _scroll.removeListener(_onScroll);
    _scroll.dispose();
    super.dispose();
  }

  /// 滚动近底部时加载下一页
  void _onScroll() {
    if (_scroll.position.extentAfter < 240) {
      ref.read(messagesProvider.notifier).loadMore();
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final state = ref.watch(messagesProvider);
    final status = ref.watch(connectionStatusProvider);
    return Column(
      children: [
        _FilterBar(l10n: l10n, state: state, status: status),
        if (status != ConnectionStatus.connected && status != ConnectionStatus.idle)
          _ReconnectBanner(l10n: l10n),
        Expanded(
          child: state.loading
              ? const Center(child: CircularProgressIndicator())
              : RefreshIndicator(
                  onRefresh: () =>
                      ref.read(messagesProvider.notifier).refresh(),
                  child: state.messages.isEmpty
                      ? ListView(
                          children: [
                            Padding(
                              padding: const EdgeInsets.all(48),
                              child: Center(
                                child: Text(
                                  l10n.messagesEmpty,
                                  style: TextStyle(
                                    color:
                                        context.theme.colors.mutedForeground,
                                  ),
                                ),
                              ),
                            ),
                          ],
                        )
                      : ListView.builder(
                          controller: _scroll,
                          padding: const EdgeInsets.all(16),
                          itemCount: state.messages.length +
                              (state.hasMore || state.loadingMore ? 1 : 0),
                          itemBuilder: (context, index) {
                            if (index >= state.messages.length) {
                              return const Padding(
                                padding: EdgeInsets.all(16),
                                child: Center(
                                  child: SizedBox(
                                    width: 22,
                                    height: 22,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                    ),
                                  ),
                                ),
                              );
                            }
                            return _MessageCard(
                              message: state.messages[index],
                            );
                          },
                        ),
                ),
        ),
      ],
    );
  }
}

/// 过滤栏：全部/未读切换与全部已读
class _FilterBar extends ConsumerWidget {
  final AppLocalizations l10n;

  final MessagesState state;

  final ConnectionStatus status;

  const _FilterBar({
    required this.l10n,
    required this.state,
    required this.status,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 4),
      child: Row(
        children: [
          _FilterChip(
            label: l10n.messagesFilterAll,
            selected: state.readFlag == readFlagAll,
            onTap: () => ref
                .read(messagesProvider.notifier)
                .setReadFlag(readFlagAll),
          ),
          const SizedBox(width: 8),
          _FilterChip(
            label: l10n.messagesFilterUnread,
            selected: state.readFlag == readFlagUnread,
            onTap: () => ref
                .read(messagesProvider.notifier)
                .setReadFlag(readFlagUnread),
          ),
          const Spacer(),
          TextButton(
            onPressed: () =>
                ref.read(messagesProvider.notifier).markAllRead(),
            child: Text(
              l10n.messagesMarkAllRead,
              style: TextStyle(
                fontSize: 12,
                color: context.theme.colors.mutedForeground,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// 筛选片
class _FilterChip extends StatelessWidget {
  final String label;

  final bool selected;

  final VoidCallback onTap;

  const _FilterChip({
    required this.label,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      color: selected
          ? context.theme.colors.primary
          : context.theme.colors.secondary,
      borderRadius: BorderRadius.circular(999),
      child: InkWell(
        borderRadius: BorderRadius.circular(999),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 7),
          child: Text(
            label,
            style: TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w500,
              color: selected
                  ? context.theme.colors.primaryForeground
                  : context.theme.colors.foreground,
            ),
          ),
        ),
      ),
    );
  }
}

/// 断连横幅（手动重连入口）
class _ReconnectBanner extends ConsumerWidget {
  final AppLocalizations l10n;

  const _ReconnectBanner({required this.l10n});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Container(
      margin: const EdgeInsets.fromLTRB(16, 4, 16, 4),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: const Color(0xFFD97706).withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Row(
        children: [
          const Icon(Icons.wifi_off_rounded, size: 16, color: Color(0xFFD97706)),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              l10n.messagesDisconnected,
              style: const TextStyle(fontSize: 12, color: Color(0xFF92400E)),
            ),
          ),
          TextButton(
            onPressed: () => ref.read(tbsClientProvider).sse.manualReconnect(),
            child: Text(l10n.messagesReconnect),
          ),
        ],
      ),
    );
  }
}

/// 消息卡片（优先级色条 + 未读点 + 已读操作 + 详情）
class _MessageCard extends ConsumerWidget {
  final PushNotifyMessage message;

  const _MessageCard({required this.message});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final priorityColor = switch (message.priority) {
      'urgent' => const Color(0xFFDC2626),
      'high' => const Color(0xFFD97706),
      'low' => const Color(0xFF71717A),
      _ => const Color(0xFF2563EB),
    };
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      decoration: BoxDecoration(
        color: context.theme.colors.card,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: context.theme.colors.border),
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(12),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Container(width: 3, color: priorityColor),
            Expanded(
              child: InkWell(
                onTap: () => _openDetail(context, ref, l10n),
                child: Padding(
                  padding: const EdgeInsets.all(14),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          if (!message.readFlag)
                            Container(
                              width: 7,
                              height: 7,
                              margin: const EdgeInsets.only(right: 8),
                              decoration: BoxDecoration(
                                color: context.theme.colors.primary,
                                shape: BoxShape.circle,
                              ),
                            ),
                          Expanded(
                            child: Text(
                              message.title ?? '',
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: TextStyle(
                                fontWeight: message.readFlag
                                    ? FontWeight.w400
                                    : FontWeight.w600,
                              ),
                            ),
                          ),
                          _TypeBadge(msgType: message.msgType ?? ''),
                        ],
                      ),
                      const SizedBox(height: 6),
                      Text(
                        message.content ?? '',
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                          fontSize: 13,
                          color: context.theme.colors.mutedForeground,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          Text(
                            message.createTime ?? '',
                            style: TextStyle(
                              fontSize: 11,
                              color: context.theme.colors.mutedForeground,
                            ),
                          ),
                          const Spacer(),
                          if (!message.readFlag)
                            TextButton(
                              onPressed: () => ref
                                  .read(messagesProvider.notifier)
                                  .markRead([message.messageId]),
                              child: Text(
                                l10n.messagesMarkRead,
                                style: const TextStyle(fontSize: 12),
                              ),
                            ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// 打开详情弹层并回传已读
  Future<void> _openDetail(
    BuildContext context,
    WidgetRef ref,
    AppLocalizations l10n,
  ) async {
    final controller = ref.read(messagesProvider.notifier);
    if (!message.readFlag) {
      controller.markRead([message.messageId]);
    }
    await showModalBottomSheet<void>(
      context: context,
      showDragHandle: true,
      builder: (sheetContext) => Padding(
        padding: const EdgeInsets.fromLTRB(20, 0, 20, 24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              message.title ?? '',
              style: sheetContext.theme.typography.body.lg
                  .copyWith(fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 12),
            Text(message.content ?? ''),
            const SizedBox(height: 16),
            Text(
              '${l10n.messagesSender}：${message.sender ?? '-'} · ${message.createTime ?? '-'}',
              style: TextStyle(
                fontSize: 12,
                color: sheetContext.theme.colors.mutedForeground,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// 消息类型徽标
class _TypeBadge extends StatelessWidget {
  final String msgType;

  const _TypeBadge({required this.msgType});

  @override
  Widget build(BuildContext context) {
    if (msgType.isEmpty) {
      return const SizedBox.shrink();
    }
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: context.theme.colors.secondary,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        msgType,
        style: TextStyle(
          fontSize: 10,
          color: context.theme.colors.mutedForeground,
        ),
      ),
    );
  }
}
