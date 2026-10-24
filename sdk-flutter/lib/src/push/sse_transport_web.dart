/// SSE 下行流传输（Web 平台实现）
///
/// 经 fetch_client 走浏览器 fetch 流式读取（ReadableStream），支持跨域与
/// 凭证携带；浏览器环境下连接存活由服务端与代理层管理。
library;

import 'dart:convert';

import 'package:fetch_client/fetch_client.dart';
import 'package:http/http.dart' as http;

import '../exception/tbs_exceptions.dart';

/// SSE 传输抽象
abstract class SseTransport {
  /// 建立 SSE 下行流
  ///
  /// @param url     建连地址（含 push-ticket 参数）
  /// @param headers 请求头（携带 Authorization 令牌）
  /// @return UTF-8 解码后的文本行流
  Stream<String> connect(Uri url, Map<String, String> headers);

  /// 关闭底层连接（幂等）
  void close();
}

/// 创建平台 SSE 传输（Web 实现）
///
/// @return SSE 传输实例
SseTransport createSseTransport() => _WebSseTransport();

class _WebSseTransport implements SseTransport {
  http.Client? _client;

  @override
  Stream<String> connect(Uri url, Map<String, String> headers) async* {
    final client = FetchClient(
      mode: RequestMode.cors,
      credentials: RequestCredentials.cors,
      streamRequests: true,
    );
    _client = client;
    final request = http.Request('GET', url)
      ..headers['Accept'] = 'text/event-stream'
      ..headers['Cache-Control'] = 'no-cache'
      ..headers.addAll(headers);
    final response = await client.send(request);
    if (response.statusCode != 200) {
      client.close();
      _client = null;
      throw TbsClientException('SSE 建连失败（HTTP ${response.statusCode}）');
    }
    yield* response.stream
        .transform(const Utf8Decoder(allowMalformed: true))
        .transform(const LineSplitter());
  }

  @override
  void close() {
    _client?.close();
    _client = null;
  }
}
