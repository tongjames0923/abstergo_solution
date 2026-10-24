/// 推送协议帧类型
///
/// 对应服务端 FrameTypeEnum：枚举名与服务端一致（JSON 序列化为大写名），
/// value 为小写值（与 SSE 事件名对应）。SIGNAL 为服务端下发信令帧，客户端禁止上行。
enum FrameType {
  /// 握手确认帧（服务端下发，载荷含 sessionId 与协议版本）
  hello,

  /// 心跳请求帧
  ping,

  /// 心跳应答帧（服务端下发）
  pong,

  /// 数据帧（业务消息文本）
  data,

  /// 信令帧（服务端下发，通知客户端拉取）
  signal,

  /// 订阅帧（客户端上行）
  subscribe,

  /// 退订帧（客户端上行）
  unsubscribe,

  /// 错误帧
  error, shutdown;

  /// 帧类型小写值（与 SSE 事件名对应）
  ///
  /// @return 小写枚举名
  String get wireName => name;

  /// 上行帧 JSON 序列化值（大写枚举名，对齐服务端 FrameTypeEnum 反序列化约定）
  ///
  /// @return 大写枚举名
  String get wireEnumName => name.toUpperCase();

  /// 从 SSE 事件名或 JSON 值解析帧类型（兼容 push- 前缀事件名）
  ///
  /// @param value 事件名/序列化值
  /// @return 帧类型，未知值时为 null
  static FrameType? tryParse(String value) {
    final normalized = value.toLowerCase();
    if (normalized.startsWith('push-')) {
      final name = FrameType.values.where((e) => e.wireName == normalized.substring(5));
      return name.isEmpty ? null : name.first;
    }
    final matches = FrameType.values.where((e) => e.wireName == normalized);
    return matches.isEmpty ? null : matches.first;
  }

  /// 从上行 JSON 大写枚举名解析帧类型
  ///
  /// @param value 大写枚举名
  /// @return 帧类型，未知值时为 null
  static FrameType? tryParseEnumName(String value) {
    final normalized = value.toLowerCase();
    final matches = FrameType.values.where((e) => e.name == normalized);
    return matches.isEmpty ? null : matches.first;
  }
}
