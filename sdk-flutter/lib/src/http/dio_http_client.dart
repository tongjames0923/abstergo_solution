import 'dart:async';

import 'package:dio/dio.dart';
import 'package:logging/logging.dart';

import '../auth/token_store.dart';
import '../config/tbs_client_config.dart';
import '../constants/tbs_constants.dart' as c;
import '../exception/tbs_exceptions.dart';
import '../platform/file_downloader.dart';
import '../platform/http_adapter_setup.dart';
import 'ajax_response.dart';
import 'base_http_client.dart';
import 'client_events.dart';
import 'request_options.dart';
import 'session_refresh.dart';

/// 统一 HTTP 客户端（dio 实现）
///
/// 对齐 Java 客户端 OkHttpBaseHttpClient 语义：拦截器注入令牌与语言头；
/// 统一解析 AjaxResult 并按 SessionPolicy 判定业务码——成功返回（携带
/// message+type 或 701 时发出通知事件、携带 redirect 时发出跳转事件）、
/// 刷新码走单飞刷新并重放原请求、重登码先发会话失效事件再抛异常、
/// 其余非成功码抛服务端异常；隔离型请求跳过登录态处理直接返回原始信封。
class DioHttpClient implements BaseHttpClient {
  static final Logger _log = Logger('tbs_client.http');

  static const String _extraOptions = 'tbs_options';

  static const String _extraReplay = 'tbs_replay';

  final TbsClientConfig _config;

  final TokenStore _tokenStore;

  final Dio _dio;

  final StreamController<TbsClientEvent> _events =
      StreamController<TbsClientEvent>.broadcast();

  SessionRefreshCoordinator? _refreshCoordinator;

  bool _closed = false;

  /// 创建 HTTP 客户端
  ///
  /// @param config     客户端配置
  /// @param tokenStore 令牌存储
  /// @param dio        外部注入的 dio 实例（测试缝隙，缺省自建）
  DioHttpClient(this._config, this._tokenStore, {Dio? dio}) : _dio = dio ?? Dio() {
    _dio.options
      ..baseUrl = _config.baseUrl
      ..connectTimeout = _config.connectTimeout
      ..receiveTimeout = _config.receiveTimeout
      ..headers['Accept-Language'] = _config.language
      ..responseType = ResponseType.json;
    _dio.interceptors.add(
      InterceptorsWrapper(onRequest: _injectHeaders),
    );
    configureHttpClientAdapter(
      _dio,
      withCredentials: _config.sessionPolicy.refreshCodes.isNotEmpty,
    );
    if (_config.sessionPolicy.refreshCodes.isNotEmpty) {
      _refreshCoordinator = SessionRefreshCoordinator(
        perform: _performRefresh,
        tokenStore: _tokenStore,
      );
    }
  }

  @override
  Future<AjaxResponse> send(
    String method,
    String path, {
    Object? body,
    Map<String, dynamic>? query,
    TbsRequestOptions options = TbsRequestOptions.defaults,
  }) {
    return _execute(method, path, body, query, options, isReplay: false);
  }

  @override
  Future<AjaxResponse> get(
    String path, {
    Map<String, dynamic>? query,
    TbsRequestOptions options = TbsRequestOptions.defaults,
  }) {
    return send('GET', path, query: query, options: options);
  }

  @override
  Future<AjaxResponse> post(
    String path, {
    Object? body,
    Map<String, dynamic>? query,
    TbsRequestOptions options = TbsRequestOptions.defaults,
  }) {
    return send('POST', path, body: body, query: query, options: options);
  }

  @override
  Future<AjaxResponse> put(
    String path, {
    Object? body,
    TbsRequestOptions options = TbsRequestOptions.defaults,
  }) {
    return send('PUT', path, body: body, options: options);
  }

  @override
  Future<AjaxResponse> delete(
    String path, {
    Object? body,
    Map<String, dynamic>? query,
    TbsRequestOptions options = TbsRequestOptions.defaults,
  }) {
    return send('DELETE', path, body: body, query: query, options: options);
  }

  @override
  Future<void> download(
    String url,
    String savePath, {
    void Function(int received, int total)? onProgress,
    Map<String, String> headers = const {},
  }) {
    return downloadToFile(url, savePath, onProgress: onProgress, headers: headers);
  }

  @override
  String? get currentToken => _tokenStore.peek();

  @override
  Stream<TbsClientEvent> get events => _events.stream;

  @override
  Future<void> close() async {
    if (_closed) {
      return;
    }
    _closed = true;
    _dio.close();
    await _events.close();
  }

  /// 执行请求并处理响应（含刷新重放）
  ///
  /// @param method   HTTP 动词
  /// @param path     接口路径
  /// @param body     请求体
  /// @param query    查询参数
  /// @param options  请求级选项
  /// @param isReplay 是否为刷新后的重放请求（重放再遇刷新码直接判会话失效）
  /// @return 统一响应信封
  Future<AjaxResponse> _execute(
    String method,
    String path,
    Object? body,
    Map<String, dynamic>? query,
    TbsRequestOptions options, {
    required bool isReplay,
  }) async {
    if (_closed) {
      throw const TbsClientException('客户端已释放，无法发起请求');
    }
    Response<dynamic> response;
    try {
      response = await _dio.request<dynamic>(
        path,
        data: body,
        queryParameters: query,
        options: Options(
          method: method,
          headers: options.headers.isEmpty ? null : options.headers,
          extra: <String, Object?>{
            _extraOptions: options,
            _extraReplay: isReplay,
          },
          receiveTimeout: options.receiveTimeout,
          sendTimeout: body == null ? null : _config.sendTimeout,
        ),
      );
    } on DioException catch (e) {
      final badBody = e.type == DioExceptionType.badResponse &&
          e.response?.data is Map;
      if (badBody) {
        final envelope = _parseBody(e.response!.data as Object);
        return _handleEnvelope(
          envelope,
          method,
          path,
          body,
          query,
          options,
          isReplay: isReplay,
        );
      }
      throw TbsClientException(_describeDioError(e), cause: e);
    }
    final envelope = _parseBody(response.data);
    return _handleEnvelope(
      envelope,
      method,
      path,
      body,
      query,
      options,
      isReplay: isReplay,
    );
  }

  /// 注入令牌与语言请求头
  ///
  /// @param options 请求配置
  /// @param handler 拦截器处理器
  Future<void> _injectHeaders(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final tbsOptions =
        options.extra[_extraOptions] as TbsRequestOptions? ??
            TbsRequestOptions.defaults;
    final headerName = _config.tokenHeaderName;
    final explicitHeader = options.headers[headerName] as String?;
    if (explicitHeader == null || explicitHeader.isEmpty) {
      final token = tbsOptions.explicitToken ?? _tokenStore.peek();
      if (token != null && token.isNotEmpty) {
        options.headers[headerName] = token;
      }
    }
    handler.next(options);
  }

  /// 解析响应体为统一信封
  ///
  /// @param body 响应体
  /// @return 统一响应信封
  AjaxResponse _parseBody(Object? body) {
    if (body is! Map) {
      throw const TbsClientException('响应结构不符合 AjaxResult 协议');
    }
    final json = Map<String, dynamic>.from(body);
    final envelope = AjaxResponse.fromJson(json);
    if (!json.containsKey('code')) {
      throw const TbsClientException('响应缺少业务码字段，不符合 AjaxResult 协议');
    }
    return envelope;
  }

  /// 按会话策略处理响应信封
  ///
  /// @param envelope 统一响应信封
  /// @param method   HTTP 动词（重放用）
  /// @param path     接口路径（重放用）
  /// @param body     请求体（重放用）
  /// @param query    查询参数（重放用）
  /// @param options  请求级选项
  /// @param isReplay 是否为重放请求
  /// @return 统一响应信封
  Future<AjaxResponse> _handleEnvelope(
    AjaxResponse envelope,
    String method,
    String path,
    Object? body,
    Map<String, dynamic>? query,
    TbsRequestOptions options, {
    required bool isReplay,
  }) async {
    if (options.skipAuthHandling) {
      return envelope;
    }
    if (options.isRefreshRequest &&
        (_config.sessionPolicy.requiresRelogin(envelope.code) ||
            _config.sessionPolicy.allowsRefresh(envelope.code))) {
      throw TbsReloginRequiredException(
        envelope.message,
        code: envelope.code,
      );
    }
    if (envelope.isSuccess) {
      _emitSuccessExtras(envelope);
      return envelope;
    }
    if (!isReplay &&
        _config.sessionPolicy.allowsRefresh(envelope.code) &&
        _refreshCoordinator != null) {
      final refreshed = await _refreshCoordinator!.refresh();
      if (refreshed) {
        _log.fine('令牌刷新成功，重放请求 $path');
        return _execute(method, path, body, query, options, isReplay: true);
      }
      _emitSessionExpired(envelope);
      throw TbsReloginRequiredException(envelope.message, code: envelope.code);
    }
    if (_config.sessionPolicy.requiresRelogin(envelope.code)) {
      _emitSessionExpired(envelope);
      if (envelope.code == c.codeUnauthorized) {
        throw TbsAuthException(envelope.message, code: envelope.code);
      }
      throw TbsReloginRequiredException(envelope.message, code: envelope.code);
    }
    if (envelope.code == c.codeNotice) {
      _events.add(
        NoticeEvent(
          message: envelope.message,
          type: envelope.type,
          title: envelope.title,
          duration: envelope.duration,
        ),
      );
    } else if (envelope.code == c.codeDebug) {
      _log.fine('调试信息（$path）：${envelope.message}');
    }
    if (envelope.redirect != null && envelope.redirect!.isNotEmpty) {
      _events.add(RedirectEvent(path: envelope.redirect!));
    }
    throw TbsServerException(
      envelope.message.isEmpty ? '服务端返回错误码 ${envelope.code}' : envelope.message,
      code: envelope.code,
    );
  }

  /// 发出成功响应的附加语义事件（提示与跳转）
  ///
  /// @param envelope 统一响应信封
  void _emitSuccessExtras(AjaxResponse envelope) {
    if (envelope.hasSuccessNotice) {
      _events.add(
        NoticeEvent(
          message: envelope.message,
          type: envelope.type,
          title: envelope.title,
          duration: envelope.duration,
        ),
      );
    }
    if (envelope.redirect != null && envelope.redirect!.isNotEmpty) {
      _events.add(RedirectEvent(path: envelope.redirect!));
    }
  }

  /// 发出会话失效事件
  ///
  /// @param envelope 统一响应信封
  void _emitSessionExpired(AjaxResponse envelope) {
    _events.add(
      SessionExpiredEvent(code: envelope.code, message: envelope.message),
    );
  }

  /// 执行刷新令牌请求
  ///
  /// @return 刷新响应信封（由协调器提取新令牌并落库）
  Future<AjaxResponse> _performRefresh() async {
    final response = await _dio.request<dynamic>(
      c.pathLogin + c.grantRefresh,
      options: Options(
        method: 'POST',
        extra: <String, Object?>{
          _extraOptions: refreshRequestOptions(),
          _extraReplay: true,
        },
      ),
    );
    return _parseBody(response.data);
  }

  /// 描述网络层错误
  ///
  /// @param error dio 异常
  /// @return 错误描述文本
  String _describeDioError(DioException error) {
    switch (error.type) {
      case DioExceptionType.connectionTimeout:
        return '连接超时';
      case DioExceptionType.receiveTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.transformTimeout:
        return '请求超时';
      case DioExceptionType.cancel:
        return '请求已取消';
      case DioExceptionType.badCertificate:
        return '证书校验失败';
      case DioExceptionType.connectionError:
        return '网络连接失败';
      case DioExceptionType.badResponse:
        return '服务端响应异常（HTTP ${error.response?.statusCode ?? '未知'}）';
      case DioExceptionType.unknown:
        return '网络请求失败';
    }
  }
}
