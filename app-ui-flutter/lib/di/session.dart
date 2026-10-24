import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:tbs_client/tbs_client.dart';

import 'providers.dart';

/// 会话状态 Provider（AsyncNotifier，null 表示未登录）
final sessionProvider = AsyncNotifierProvider<SessionController, UserInfoModel?>(
  SessionController.new,
);

/// SSE 连接状态 Provider
final connectionStatusProvider =
    StateProvider<ConnectionStatus>((ref) => ConnectionStatus.idle);

/// 未读消息数 Provider
final unreadCountProvider = NotifierProvider<UnreadCountController, int>(
  UnreadCountController.new,
);

/// 会话控制器：恢复会话、登录、部门切换、登出与 SSE 装配
class SessionController extends AsyncNotifier<UserInfoModel?> {
  @override
  Future<UserInfoModel?> build() async {
    final client = ref.watch(tbsClientProvider);
    if (client.currentToken == null) {
      return null;
    }
    try {
      final user = await client.auth.userInfo();
      _startPush(user);
      return user;
    } on TbsException {
      await ref.read(tokenStoreProvider).clear();
      return null;
    }
  }

  /// 账号密码登录
  ///
  /// @param username   用户名
  /// @param password   密码
  /// @param rememberMe 记住我（Web 端触发种刷新 Cookie）
  Future<void> loginPassword(
    String username,
    String password, {
    bool rememberMe = false,
  }) async {
    final client = ref.read(tbsClientProvider);
    await client.auth.loginByPassword(
      username,
      password,
      rememberMe: rememberMe,
    );
    final user = await client.auth.userInfo();
    state = AsyncData(user);
    _startPush(user);
  }

  /// 企微 SSO 授权码登录
  ///
  /// @param code 企微授权码
  Future<void> loginWecom(String code) async {
    final client = ref.read(tbsClientProvider);
    await client.auth.loginByWecom(code);
    final user = await client.auth.userInfo();
    state = AsyncData(user);
    _startPush(user);
  }

  /// 第三方应用授权登录
  ///
  /// @param appId     应用标识
  /// @param appSecret 应用密钥（仅本地生成授权码）
  Future<void> loginThirdParty(String appId, String appSecret) async {
    final client = ref.read(tbsClientProvider);
    await client.auth.loginByThirdParty(appId, appSecret);
    final user = await client.auth.userInfo();
    state = AsyncData(user);
    _startPush(user);
  }

  /// 切换当前部门并刷新权限上下文
  ///
  /// @param departmentId 部门 ID（字符串形态）
  Future<void> switchDepartment(String departmentId) async {
    final client = ref.read(tbsClientProvider);
    await client.auth.switchDepartment(departmentId);
    final user = await client.auth.userInfo();
    state = AsyncData(user);
  }

  /// 登出：后端注销 + 关闭 SSE + 本地清理
  Future<void> logout() async {
    final client = ref.read(tbsClientProvider);
    ref.read(connectionStatusProvider.notifier).state = ConnectionStatus.idle;
    ref.read(unreadCountProvider.notifier).reset();
    await client.logout();
    state = const AsyncData(null);
  }

  /// 启动 SSE 推送订阅并桥接状态
  ///
  /// @param user 当前用户
  void _startPush(UserInfoModel user) {
    if (user.id == null) {
      return;
    }
    final client = ref.read(tbsClientProvider);
    client.sse.setListener(_SseBridge(ref));
    client.sse.start('${user.id}');
    ref.read(unreadCountProvider.notifier).refresh();
  }
}

/// SSE 监听桥：状态与信令映射到 Riverpod
class _SseBridge extends SseFrameListener {
  final Ref _ref;

  /// 创建桥接器
  ///
  /// @param ref Riverpod 引用
  _SseBridge(this._ref);

  @override
  void onStatusChanged(ConnectionStatus status) {
    _ref.read(connectionStatusProvider.notifier).state = status;
  }

  @override
  void onSignal() {
    _ref.read(unreadCountProvider.notifier).refresh();
  }
}

/// 未读数控制器
class UnreadCountController extends Notifier<int> {
  @override
  int build() => 0;

  /// 刷新未读数（SSE 信令/页面进入时调用）
  Future<void> refresh() async {
    final client = ref.read(tbsClientProvider);
    try {
      state = await client.push.unreadCount();
    } on TbsException {
      state = 0;
    }
  }

  /// 重置未读数（登出时调用）
  void reset() {
    state = 0;
  }
}
