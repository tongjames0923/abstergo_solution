import 'dart:async';

import 'package:logging/logging.dart';

import '../auth/token_store.dart';
import '../config/tbs_client_config.dart';
import '../constants/tbs_constants.dart' as c;
import '../exception/tbs_exceptions.dart';
import '../http/base_http_client.dart';
import '../model/frame_type.dart';
import '../model/push_credential.dart';
import '../model/push_frame.dart';
import 'sse_frame_codec.dart';
import 'sse_transport.dart';

part 'sse_push_subscriber_frames.dart';

/// 推送连接状态
enum ConnectionStatus {
  /// 未启动
  idle,

  /// 建连中（签票 + 建流 + 握手）
  connecting,

  /// 在线（hello 握手完成）
  connected,

  /// 重连中（断线退避换票重连）
  reconnecting,

  /// 已关闭（手动关闭或超过最大自动重连次数）
  closed,
}

/// SSE 帧监听器
///
/// 回调在订阅器单线程事件循环中顺序执行且回调异常被隔离；耗时处理请由宿主
/// 自行转交业务执行器。全部方法提供空默认实现，宿主按需覆写。
abstract class SseFrameListener {
  /// 连接状态变化
  ///
  /// @param status 最新状态
  void onStatusChanged(ConnectionStatus status) {}

  /// 握手就绪（hello 帧确认 sessionId）
  ///
  /// @param hello 握手信息
  void onHello(HelloInfo hello) {}

  /// 信令（无载荷，提示频道有新消息，宿主据此拉取）
  void onSignal() {}

  /// 业务帧（data/pong 等帧分发）
  ///
  /// @param type  帧类型
  /// @param frame 协议帧
  void onFrame(FrameType type, PushFrame frame) {}

  /// 异常通知（建连失败/服务端错误帧/重连超限等）
  ///
  /// @param error 异常对象
  void onError(Object error) {}
}

/// SSE 推送订阅器（状态机）
///
/// 编排对齐 Java 客户端 SsePushSubscriber 与前端 push 模块：
/// 1. 签票：POST /private/push/auth（携登录令牌）签发一次性 push-ticket；
/// 2. 建连：SSE 下行流携 push-ticket 与 Authorization 令牌头；
/// 3. 握手：hello 帧 payload 二次解析 sessionId（以握手帧为准），就绪后统一
///    上行 SUBSCRIBE 全部已登记主题、启动心跳、回调 onHello 并触发一次
///    onSignal 兜底拉取；
/// 4. 信令：signal 事件回调 onSignal；pong 重置心跳失败计数；
/// 5. 动态订阅：连接就绪时立即上行帧，未就绪时仅登记，待 hello 后统一订阅；
/// 6. 心跳：按周期上行 PING 帧，连续失败达阈值判定会话已死，主动关流换票重连；
/// 7. 重连：断线指数退避（min(base×2^n, max)+抖动），超过最大次数转手动重连；
/// 8. 释放：close() 幂等，取消心跳与重连并关闭下行流。
class SsePushSubscriber {
  static final Logger _log = Logger('tbs_client.sse');

  final BaseHttpClient _httpClient;

  final TokenStore _tokenStore;

  final TbsClientConfig _config;

  final SseTransport _transport = createSseTransport();

  SseFrameListener? _listener;

  ConnectionStatus _status = ConnectionStatus.idle;

  /// 已登记订阅主题（含缺省 per-user 频道）
  final Set<String> _topics = <String>{};

  /// 当前会话标识（hello 帧下发）
  String? _sessionId;

  /// 签票 userId
  String? _userId;

  Timer? _heartbeatTimer;

  bool _pongSinceLastPing = true;

  int _heartbeatFailures = 0;

  int _reconnectAttempts = 0;

  bool _manualClose = false;

  bool _disposed = false;

  bool _loopActive = false;

  bool _handshaked = false;

  /// 创建 SSE 订阅器
  ///
  /// @param httpClient 统一 HTTP 客户端
  /// @param tokenStore 令牌存储
  /// @param config     客户端配置
  SsePushSubscriber({
    required BaseHttpClient httpClient,
    required TokenStore tokenStore,
    required TbsClientConfig config,
  })  : _httpClient = httpClient,
        _tokenStore = tokenStore,
        _config = config;

  /// 当前连接状态
  ///
  /// @return 连接状态
  ConnectionStatus get status => _status;

  /// 当前会话标识（握手完成后可用）
  ///
  /// @return 会话标识，未握手时为 null
  String? get sessionId => _sessionId;

  /// 注册帧监听器
  ///
  /// @param listener 帧监听器
  void setListener(SseFrameListener listener) {
    _listener = listener;
  }

  /// 启动订阅（登记 per-user 缺省频道并建连）
  ///
  /// @param userId 用户数字 id（签票与会话属性注入）
  void start(String userId) {
    if (_disposed || userId.isEmpty) {
      return;
    }
    _userId = userId;
    _topics.add('${_config.pushOptions.topicPrefix}$userId');
    _manualClose = false;
    if (!_loopActive) {
      _runLoop();
    }
  }

  /// 动态订阅主题（就绪时立即上行，未就绪仅登记待 hello 后统一订阅）
  ///
  /// @param topics 主题列表
  void subscribeTopics(List<String> topics) {
    for (final topic in topics) {
      if (topic.isEmpty || topic.length > c.topicMaxLength) {
        continue;
      }
      _topics.add(topic);
      if (_handshaked && _sessionId != null) {
        _sendFrame(FrameUploadRequest.subscribe(
          _sessionId!,
          _config.pushOptions.protocolVersion,
          topic,
        ));
      }
    }
  }

  /// 动态退订主题
  ///
  /// @param topics 主题列表
  void unsubscribeTopics(List<String> topics) {
    for (final topic in topics) {
      _topics.remove(topic);
      if (_handshaked && _sessionId != null) {
        _sendFrame(FrameUploadRequest.unsubscribe(
          _sessionId!,
          _config.pushOptions.protocolVersion,
          topic,
        ));
      }
    }
  }

  /// 手动重连（超过最大自动重连次数后由宿主调用）
  void manualReconnect() {
    if (_disposed || _loopActive) {
      return;
    }
    _reconnectAttempts = 0;
    _manualClose = false;
    _runLoop();
  }

  /// 关闭订阅（幂等，释放后实例不可复用）
  Future<void> close() async {
    if (_disposed) {
      return;
    }
    _disposed = true;
    _manualClose = true;
    _stopHeartbeat();
    _transport.close();
    _setStatus(ConnectionStatus.closed);
  }

  /// 主循环：建连→消费→断线退避重连
  Future<void> _runLoop() async {
    if (_loopActive) {
      return;
    }
    _loopActive = true;
    try {
      while (!_disposed && !_manualClose) {
        try {
          await _establishOnce();
        } on TbsException catch (error) {
          _listener?.onError(error);
        } catch (error) {
          _listener?.onError(TbsClientException('SSE 连接异常', cause: error));
        }
        if (_disposed || _manualClose) {
          break;
        }
        _reconnectAttempts++;
        final maxAttempts = _config.pushOptions.reconnectMaxAttempts;
        if (maxAttempts > 0 && _reconnectAttempts > maxAttempts) {
          _setStatus(ConnectionStatus.closed);
          _listener?.onError(
            const TbsClientException('超过最大自动重连次数，请调用 manualReconnect 重连'),
          );
          return;
        }
        _setStatus(ConnectionStatus.reconnecting);
        final delay = _config.pushOptions.backoffFor(_reconnectAttempts - 1);
        _log.fine('SSE 第 $_reconnectAttempts 次重连，退避 ${delay.inMilliseconds}ms');
        await Future<void>.delayed(delay);
      }
    } finally {
      _loopActive = false;
    }
  }

  /// 单次建连消费（签票→建流→握手→事件分发，流结束即返回）
  Future<void> _establishOnce() async {
    _handshaked = false;
    _heartbeatFailures = 0;
    _setStatus(ConnectionStatus.connecting);
    final credential = await _requestCredential();
    final token = _tokenStore.peek() ?? '';
    final base = _config.baseUrl.endsWith('/')
        ? _config.baseUrl.substring(0, _config.baseUrl.length - 1)
        : _config.baseUrl;
    final uri = Uri.parse('$base${c.pathPushSseConnect}').replace(
      queryParameters: <String, String>{c.paramPushTicket: credential.ticket},
    );
    final lines = _transport.connect(
      uri,
      <String, String>{
        if (token.isNotEmpty) _config.tokenHeaderName: token,
      },
    );
    final events = decodeSseStream(lines);
    try {
      await for (final event in events) {
        _dispatchEvent(event);
        if (_manualClose || _disposed) {
          break;
        }
      }
    } finally {
      _stopHeartbeat();
    }
  }

  /// 签发一次性连接凭证
  ///
  /// @return 推送连接凭证
  Future<PushCredential> _requestCredential() async {
    final token = _tokenStore.peek();
    if (token == null || token.isEmpty) {
      throw const TbsAuthException('本地无登录令牌，无法签发推送凭证');
    }
    final response = await _httpClient.post(
      c.pathPushAuth,
      body: <String, dynamic>{
        'token': token,
        if (_userId != null) 'userId': _userId,
      },
    );
    final credential = PushCredential.fromJson(response.dataAsMap());
    if (credential.ticket.isEmpty) {
      throw const TbsClientException('推送签票响应缺少 ticket');
    }
    return credential;
  }
}
