import '../tbs_client.dart' show TbsClient;
import 'auth/auth_api.dart';
import 'auth/token_store.dart';
import 'config/tbs_client_config.dart';
import 'constants/tbs_constants.dart' as c;
import 'http/base_http_client.dart';
import 'http/client_events.dart';
import 'http/dio_http_client.dart';
import 'model/app_update_models.dart';
import 'push/push_message_api.dart';
import 'push/sse_push_subscriber.dart';
import 'update/update_api.dart';
import 'view/paged_api.dart';
import 'view/view_api.dart';

/// TbsClient 默认实现
///
/// 门面 → 能域 API → HTTP 基础设施三层装配，对齐 Java 客户端 TbsClient。
class TbsClientImpl implements TbsClient {
  final TbsClientConfig config;

  final TokenStore tokenStore;

  final BaseHttpClient _httpClient;

  late final AuthApi _auth = _buildAuth();

  late final PushMessageApi _push = PushMessageApi(httpClient: _httpClient);

  late final SsePushSubscriber _sse = SsePushSubscriber(
    httpClient: _httpClient,
    tokenStore: tokenStore,
    config: config,
  );

  late final UpdateApi _update = UpdateApi(httpClient: _httpClient);

  late final ViewApi _view = ViewApi(httpClient: _httpClient);

  late final PagedApi _paged = PagedApi(httpClient: _httpClient);

  bool _disposed = false;

  /// 创建默认实现
  ///
  /// @param config     客户端配置
  /// @param tokenStore 令牌存储
  TbsClientImpl({required this.config, required this.tokenStore})
      : _httpClient = DioHttpClient(config, tokenStore);

  /// 构建认证能域（显式引入实现类型）
  AuthApi _buildAuth() {
    final authApi = AuthApi(httpClient: _httpClient, tokenStore: tokenStore);
    return authApi;
  }

  @override
  AuthApi get auth => _auth;

  @override
  PushMessageApi get push => _push;

  @override
  SsePushSubscriber get sse => _sse;

  @override
  UpdateApi get update => _update;

  @override
  ViewApi get view => _view;

  @override
  PagedApi get paged => _paged;

  @override
  Stream<SessionExpiredEvent> get onSessionExpired =>
      _httpClient.events.where((e) => e is SessionExpiredEvent).cast<SessionExpiredEvent>();

  @override
  Stream<NoticeEvent> get onNotice =>
      _httpClient.events.where((e) => e is NoticeEvent).cast<NoticeEvent>();

  @override
  Stream<RedirectEvent> get onRedirect =>
      _httpClient.events.where((e) => e is RedirectEvent).cast<RedirectEvent>();

  @override
  String? get currentToken => _httpClient.currentToken;

  @override
  Future<GatewayVersionModel> gatewayVersion() async {
    final response = await _httpClient.get(c.pathGatewayVersion);
    return GatewayVersionModel.fromJson(response.dataAsMap());
  }

  @override
  Future<String> gatewayVersionString() async {
    final model = await gatewayVersion();
    return model.version ?? '';
  }

  @override
  Future<void> logout() async {
    try {
      await _sse.close();
    } finally {
      await _auth.logout();
    }
  }

  @override
  Future<void> dispose() async {
    if (_disposed) {
      return;
    }
    _disposed = true;
    await _sse.close();
    await _httpClient.close();
  }
}
