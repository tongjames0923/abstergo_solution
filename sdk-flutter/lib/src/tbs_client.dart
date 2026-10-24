import 'auth/auth_api.dart';
import 'auth/token_store.dart';
import 'config/tbs_client_config.dart';
import 'http/client_events.dart';
import 'model/app_update_models.dart';
import 'push/push_message_api.dart';
import 'push/sse_push_subscriber.dart';
import 'tbs_client_impl.dart';
import 'update/update_api.dart';
import 'view/paged_api.dart';
import 'view/view_api.dart';

/// 客户端顶层门面
///
/// 单实例全局使用（内置连接池复用），多线程/并发调用安全；会话失效统一经
/// [onSessionExpired] 事件处置；SSE 回调在订阅器单线程事件循环中分发。
abstract class TbsClient {
  /// 按基础地址创建客户端
  ///
  /// @param baseUrl    服务端基础地址（必填）
  /// @param config     客户端配置（缺省按原生端会话策略）
  /// @param tokenStore 令牌存储（缺省内存实现，宿主注入平台持久化实现）
  /// @return 客户端实例
  static TbsClient create(
    String baseUrl, {
    TbsClientConfig? config,
    TokenStore? tokenStore,
  }) {
    return TbsClientImpl(
      config: config ?? TbsClientConfig.forNative(baseUrl),
      tokenStore: tokenStore ?? MemoryTokenStore(),
    );
  }

  /// 认证与用户能域
  AuthApi get auth;

  /// 推送消息中心能域
  PushMessageApi get push;

  /// SSE 订阅器（连接状态机）
  SsePushSubscriber get sse;

  /// 应用更新能域
  UpdateApi get update;

  /// 动态视图能域（管理端迁移预留）
  ViewApi get view;

  /// 泛化分页查询能域（管理端迁移预留）
  PagedApi get paged;

  /// 会话失效事件流（401/405/468 经 SessionPolicy 判定后触发，宿主据此引导重登）
  Stream<SessionExpiredEvent> get onSessionExpired;

  /// 通知事件流（成功响应携带 message+type、业务码 701 时触发）
  Stream<NoticeEvent> get onNotice;

  /// 跳转事件流（响应携带 redirect 字段时触发，宿主据此执行路由跳转）
  Stream<RedirectEvent> get onRedirect;

  /// 当前登录令牌
  ///
  /// @return 令牌串，未登录时为 null
  String? get currentToken;

  /// 查询网关版本信息
  ///
  /// @return 网关版本模型
  Future<GatewayVersionModel> gatewayVersion();

  /// 查询网关版本号字符串（便捷方法）
  ///
  /// @return 版本号，缺失时为空串
  Future<String> gatewayVersionString() async {
    final model = await gatewayVersion();
    return model.version ?? '';
  }

  /// 登出：关闭 SSE + 后端注销令牌 + 本地清理令牌（后端失败不阻断本地清理）
  Future<void> logout();

  /// 释放 SSE 连接与 HTTP 连接池（幂等，释放后实例不可复用）
  Future<void> dispose();
}
