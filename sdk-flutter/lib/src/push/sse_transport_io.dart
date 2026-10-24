/// SSE 下行流传输（native 平台实现）
///
/// 经 package:http IOClient 建立流式 GET（Accept: text/event-stream），
/// 响应体持续可读直至服务端关流；连接无读超时限制，长连接空闲不被切断。
library;

import 'dart:convert';

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

/// 创建平台 SSE 传输（native 实现）
///
/// @return SSE 传输实例
SseTransport createSseTransport() => _IoSseTransport();

class _IoSseTransport implements SseTransport {
  http.Client? _client;

  @override
  Stream<String> connect(Uri url, Map<String, String> headers) async* {
    final client = http.Client();
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
