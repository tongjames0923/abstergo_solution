/// SSE 行协议编解码
///
/// 服务端下行事件流为标准 SSE 文本协议（event/data/id 字段 + 空行分发），
/// 本模块把原始文本行流解码为结构化事件；上行帧经 PushFrame/FrameUploadRequest
/// 以 JSON 走 HTTP 上行端点，不占用下行通道。
library;

import 'dart:convert';

/// SSE 下行事件
class SseServerEvent {
  /// 事件名（小写帧类型，未显式标注时为 message）
  final String event;

  /// 累积的 data 载荷（多行以换行拼接）
  final String data;

  /// 事件 id（可选）
  final String? id;

  /// 创建下行事件
  const SseServerEvent({required this.event, required this.data, this.id});

  /// 解析载荷为 JSON 对象（payload 二次解析约定）
  ///
  /// @return 载荷 JSON Map，解析失败时为 null
  Map<String, dynamic>? tryDataAsJson() {
    if (data.isEmpty) {
      return null;
    }
    try {
      final decoded = jsonDecode(data);
      return decoded is Map ? Map<String, dynamic>.from(decoded) : null;
    } on FormatException {
      return null;
    }
  }
}

/// 将原始文本行流解码为 SSE 事件流
///
/// @param lines UTF-8 解码后的文本行流
/// @return SSE 事件流
Stream<SseServerEvent> decodeSseStream(Stream<String> lines) async* {
  var event = 'message';
  final data = <String>[];
  String? id;
  await for (final line in lines) {
    if (line.isEmpty) {
      if (data.isNotEmpty) {
        yield SseServerEvent(event: event, data: data.join('\n'), id: id);
      }
      event = 'message';
      data.clear();
      continue;
    }
    if (line.startsWith(':')) {
      continue;
    }
    final colon = line.indexOf(':');
    final field = colon < 0 ? line : line.substring(0, colon);
    var value = colon < 0 ? '' : line.substring(colon + 1);
    if (value.startsWith(' ')) {
      value = value.substring(1);
    }
    switch (field) {
      case 'event':
        event = value;
      case 'data':
        data.add(value);
      case 'id':
        id = value;
      default:
        break;
    }
  }
  if (data.isNotEmpty) {
    yield SseServerEvent(event: event, data: data.join('\n'), id: id);
  }
}
