/// SSE 下行流传输（不支持平台桩实现）
library;

/// SSE 传输抽象
abstract class SseTransport {
  /// 建立 SSE 下行流
  ///
  /// @param url             建连地址（含 push-ticket 参数）
  /// @param headers         请求头（携带 Authorization 令牌）
  /// @param withCredentials 是否携带跨域凭证（Web Cookie 刷新流）
  /// @return UTF-8 解码后的文本行流
  Stream<String> connect(
    Uri url,
    Map<String, String> headers, {
    bool withCredentials = false,
  });

  /// 关闭底层连接（幂等）
  void close();
}

/// 创建平台 SSE 传输（桩实现：抛出不支持）
///
/// @param withCredentials 是否携带跨域凭证
/// @return SSE 传输实例
SseTransport createSseTransport({bool withCredentials = false}) {
  throw UnsupportedError('当前平台不支持 SSE 长连接');
}
