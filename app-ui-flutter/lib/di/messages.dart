import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:tbs_client/tbs_client.dart';

import 'providers.dart';
import 'session.dart';

/// 消息中心页面状态
class MessagesState {
  /// 当前展示的消息列表
  final List<PushNotifyMessage> messages;

  /// 下一页游标
  final String? cursor;

  /// 是否还有更多
  final bool hasMore;

  /// 已读筛选（all/unread）
  final String readFlag;

  /// 是否正在加载
  final bool loading;

  /// 是否正在加载更多
  final bool loadingMore;

  /// 创建页面状态
  const MessagesState({
    required this.messages,
    this.cursor,
    this.hasMore = false,
    this.readFlag = readFlagAll,
    this.loading = false,
    this.loadingMore = false,
  });

  /// 复制状态
  ///
  /// @param messages    消息列表
  /// @param cursor      游标
  /// @param hasMore     是否有更多
  /// @param readFlag    已读筛选
  /// @param loading     加载中
  /// @param loadingMore 加载更多中
  /// @return 新状态
  MessagesState copyWith({
    List<PushNotifyMessage>? messages,
    String? cursor,
    bool? hasMore,
    String? readFlag,
    bool? loading,
    bool? loadingMore,
  }) =>
      MessagesState(
        messages: messages ?? this.messages,
        cursor: cursor ?? this.cursor,
        hasMore: hasMore ?? this.hasMore,
        readFlag: readFlag ?? this.readFlag,
        loading: loading ?? this.loading,
        loadingMore: loadingMore ?? this.loadingMore,
      );
}

/// 消息中心状态 Provider
final messagesProvider =
    NotifierProvider<MessagesController, MessagesState>(MessagesController.new);

/// 消息中心控制器：游标分页、筛选、已读回传与信令联动
class MessagesController extends Notifier<MessagesState> {
  /// 本地消息缓存上限（对齐 Vue 端 MAX_MESSAGES）
  static const int maxMessages = 200;

  /// 已读去重集合（messageId）
  final Set<String> _seenIds = <String>{};

  @override
  MessagesState build() {
    Future.microtask(refresh);
    return const MessagesState(messages: [], loading: true);
  }

  /// 切换已读筛选并重拉
  ///
  /// @param readFlag all/unread
  Future<void> setReadFlag(String readFlag) async {
    state = MessagesState(messages: const [], readFlag: readFlag, loading: true);
    await refresh();
  }

  /// 重新拉取首页
  Future<void> refresh() async {
    final client = ref.read(tbsClientProvider);
    final flag = state.readFlag;
    try {
      final page = await client.push.listMessages(
        PushMessageQueryForm(limit: 20, readFlag: flag),
      );
      if (flag != state.readFlag) {
        return;
      }
      _merge(page.list, prepend: false, replace: true);
      state = state.copyWith(
        cursor: page.cursor,
        hasMore: page.hasMore,
        loading: false,
      );
    } on TbsException {
      state = state.copyWith(loading: false);
    }
  }

  /// 加载下一页
  Future<void> loadMore() async {
    if (state.loadingMore || !state.hasMore || state.cursor == null) {
      return;
    }
    state = state.copyWith(loadingMore: true);
    final client = ref.read(tbsClientProvider);
    try {
      final page = await client.push.listMessages(
        PushMessageQueryForm(cursor: state.cursor, limit: 20, readFlag: state.readFlag),
      );
      _merge(page.list, prepend: false, replace: false);
      state = state.copyWith(
        cursor: page.cursor,
        hasMore: page.hasMore,
        loadingMore: false,
      );
    } on TbsException {
      state = state.copyWith(loadingMore: false);
    }
  }

  /// 批量标记已读并同步本地状态
  ///
  /// @param messageIds 消息 ID 列表
  Future<void> markRead(List<String> messageIds) async {
    if (messageIds.isEmpty) {
      return;
    }
    final client = ref.read(tbsClientProvider);
    try {
      await client.push.markRead(messageIds);
      _seenIds.addAll(messageIds);
      final updated = state.messages
          .map((m) => messageIds.contains(m.messageId)
              ? _copyRead(m)
              : m)
          .toList();
      state = state.copyWith(messages: updated);
      ref.read(unreadCountProvider.notifier).refresh();
    } on TbsException {
      state = state;
    }
  }

  /// 标记全部已读
  Future<void> markAllRead() async {
    final unreadIds = [
      for (final message in state.messages)
        if (!message.readFlag) message.messageId,
    ];
    await markRead(unreadIds);
  }

  /// 合并去重（信令触发刷新时避免重复条目）
  ///
  /// @param incoming 新消息列表
  /// @param prepend  是否前插
  /// @param replace  是否整表替换
  void _merge(
    List<PushNotifyMessage> incoming, {
    required bool prepend,
    required bool replace,
  }) {
    final fresh = [
      for (final message in incoming)
        if (!_seenIds.contains(message.messageId)) message,
    ];
    _seenIds.addAll(fresh.map((m) => m.messageId));
    var merged = replace
        ? <PushNotifyMessage>[...fresh]
        : (prepend
            ? <PushNotifyMessage>[...fresh, ...state.messages]
            : <PushNotifyMessage>[...state.messages, ...fresh]);
    if (merged.length > maxMessages) {
      merged = merged.sublist(0, maxMessages);
    }
    state = state.copyWith(messages: merged);
  }

  /// 复制消息为已读状态
  ///
  /// @param message 原消息
  /// @return 已读消息
  PushNotifyMessage _copyRead(PushNotifyMessage message) => PushNotifyMessage(
        messageId: message.messageId,
        topic: message.topic,
        msgType: message.msgType,
        title: message.title,
        content: message.content,
        priority: message.priority,
        sender: message.sender,
        url: message.url,
        createTime: message.createTime,
        readFlag: true,
        readTime: message.readTime,
        extraInfo: message.extraInfo,
      );
}
