
import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:tbs_client/tbs_client.dart';

import '../../di/providers.dart';
import '../../di/messages.dart';
import '../../di/session.dart';
import '../../shared/design/app_theme.dart';
import '../../shared/l10n/app_localizations.dart';
import '../../shared/widgets/app_nav_bar.dart';

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
    final disconnected = status != ConnectionStatus.connected &&
        status != ConnectionStatus.idle;
    return CupertinoPageScaffold(
      child: CustomScrollView(
        controller: _scroll,
        slivers: [
          AppNavBar(title: l10n.navMessages),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 12),
              child: Row(
                children: [
                  Expanded(
                    child: CupertinoSlidingSegmentedControl<String>(
                      groupValue: state.readFlag,
                      onValueChanged: (flag) {
                        if (flag != null) {
                          ref
                              .read(messagesProvider.notifier)
                              .setReadFlag(flag);
                        }
                      },
                      children: {
                        readFlagAll: Text(l10n.messagesFilterAll),
                        readFlagUnread: Text(l10n.messagesFilterUnread),
                      },
                    ),
                  ),
                  const SizedBox(width: 12),
                  CupertinoButton(
                    padding: EdgeInsets.zero,
                    minimumSize: const Size(36, 36),
                    onPressed: () =>
                        ref.read(messagesProvider.notifier).markAllRead(),
                    child: const Icon(
                      CupertinoIcons.checkmark_rectangle,
                      size: 22,
                      color: AppTheme.primary,
                    ),
                  ),
                ],
              ),
            ),
          ),
          if (disconnected)
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
                child: _ReconnectBanner(l10n: l10n),
              ),
            ),
          CupertinoSliverRefreshControl(
            onRefresh: () => ref.read(messagesProvider.notifier).refresh(),
          ),
          if (state.loading)
            const SliverFillRemaining(
              hasScrollBody: false,
              child: Center(child: CupertinoActivityIndicator()),
            )
          else if (state.messages.isEmpty)
            SliverFillRemaining(
              hasScrollBody: false,
              child: Center(
                child: Text(
                  l10n.messagesEmpty,
                  style: TextStyle(color: AppTheme.secondaryLabel(context)),
                ),
              ),
            )
          else
            SliverList.builder(
              itemCount: state.messages.length +
                  (state.hasMore || state.loadingMore ? 1 : 0),
              itemBuilder: (context, index) {
                if (index >= state.messages.length) {
                  return const Padding(
                    padding: EdgeInsets.all(16),
                    child: Center(
                      child: CupertinoActivityIndicator(),
                    ),
                  );
                }
                return Padding(
                  padding: const EdgeInsets.fromLTRB(16, 0, 16, 10),
                  child: _MessageCard(message: state.messages[index]),
                );
              },
            ),
        ],
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
    const amber = CupertinoColors.systemOrange;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: CupertinoDynamicColor.resolve(amber, context)
            .withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Row(
        children: [
          const Icon(CupertinoIcons.wifi_slash, size: 16, color: amber),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              l10n.messagesDisconnected,
              style: const TextStyle(
                fontSize: 13,
                color: amber,
              ),
            ),
          ),
          CupertinoButton(
            padding: EdgeInsets.zero,
            minimumSize: const Size(36, 36),
            onPressed: () =>
                ref.read(tbsClientProvider).sse.manualReconnect(),
            child: Text(l10n.messagesReconnect),
          ),
        ],
      ),
    );
  }
}

/// 消息卡片：优先级色条 + 未读点 + 已读操作 + 详情弹层
class _MessageCard extends ConsumerWidget {
  final PushNotifyMessage message;

  const _MessageCard({required this.message});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final priorityColor = switch (message.priority) {
      'urgent' => CupertinoColors.systemRed,
      'high' => CupertinoColors.systemOrange,
      'low' => CupertinoColors.systemGrey,
      _ => CupertinoColors.systemBlue,
    };
    final theme = CupertinoTheme.of(context);
    return GestureDetector(
      onTap: () => _openDetail(context, ref, l10n),
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: AppTheme.secondaryBackground(context),
          borderRadius: BorderRadius.circular(10),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  width: 3,
                  height: 14,
                  decoration: BoxDecoration(
                    color: CupertinoDynamicColor.resolve(
                      priorityColor,
                      context,
                    ),
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
                const SizedBox(width: 8),
                if (!message.readFlag)
                  Container(
                    width: 7,
                    height: 7,
                    margin: const EdgeInsets.only(right: 8),
                    decoration: const BoxDecoration(
                      color: AppTheme.primary,
                      shape: BoxShape.circle,
                    ),
                  ),
                Expanded(
                  child: Text(
                    message.title ?? '',
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: theme.textTheme.textStyle.copyWith(
                      fontWeight: message.readFlag
                          ? FontWeight.w400
                          : FontWeight.w600,
                    ),
                  ),
                ),
                if (message.msgType?.isNotEmpty ?? false) ...[
                  const SizedBox(width: 8),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                      vertical: 3,
                    ),
                    decoration: BoxDecoration(
                      color: theme.primaryColor.withValues(alpha: 0.10),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: Text(
                      message.msgType!,
                      style: TextStyle(
                        fontSize: 10,
                        color: AppTheme.secondaryLabel(context),
                      ),
                    ),
                  ),
                ],
              ],
            ),
            const SizedBox(height: 4),
            Text(
              message.content ?? '',
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(
                fontSize: 13,
                color: AppTheme.secondaryLabel(context),
              ),
            ),
            const SizedBox(height: 4),
            Row(
              children: [
                Text(
                  message.createTime ?? '',
                  style: TextStyle(
                    fontSize: 11,
                    color: AppTheme.secondaryLabel(context),
                  ),
                ),
                const Spacer(),
                if (!message.readFlag)
                  CupertinoButton(
                    padding: EdgeInsets.zero,
                    minimumSize: const Size(36, 36),
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
    );
  }

  /// 打开详情弹层（底部模态）并回传已读
  Future<void> _openDetail(
    BuildContext context,
    WidgetRef ref,
    AppLocalizations l10n,
  ) async {
    final controller = ref.read(messagesProvider.notifier);
    if (!message.readFlag) {
      controller.markRead([message.messageId]);
    }
    await showCupertinoModalPopup<void>(
      context: context,
      builder: (sheetContext) => Container(
        decoration: BoxDecoration(
          color: CupertinoTheme.of(sheetContext).scaffoldBackgroundColor,
          borderRadius:
              const BorderRadius.vertical(top: Radius.circular(12)),
        ),
        padding: EdgeInsets.fromLTRB(
          20,
          12,
          20,
          20 + MediaQuery.paddingOf(sheetContext).bottom,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                width: 40,
                height: 5,
                decoration: BoxDecoration(
                  color: AppTheme.separator(sheetContext),
                  borderRadius: BorderRadius.circular(3),
                ),
              ),
            ),
            const SizedBox(height: 16),
            Text(
              message.title ?? '',
              style: CupertinoTheme.of(sheetContext)
                  .textTheme
                  .navTitleTextStyle,
            ),
            const SizedBox(height: 12),
            Text(message.content ?? ''),
            const SizedBox(height: 16),
            Text(
              '${l10n.messagesSender}：${message.sender ?? '-'} · ${message.createTime ?? '-'}',
              style: TextStyle(
                fontSize: 12,
                color: AppTheme.secondaryLabel(sheetContext),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
