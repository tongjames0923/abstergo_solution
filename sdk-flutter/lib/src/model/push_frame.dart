import 'frame_type.dart';
import 'json_utils.dart';

/// 推送协议帧
///
/// 对应服务端 PushFrame：SSE 下行 data 载荷与 HTTP 上行 frame 字段的统一结构。
/// topic 为纯字符串（非空且不超过 128 字符，DATA/SUBSCRIBE/UNSUBSCRIBE 帧必填）。
class PushFrame {
  /// 协议版本（当前为 1，握手阶段版本不一致服务端拒绝连接）
  final int version;

  /// 帧类型（必填）
  final FrameType type;

  /// 消息 ID（DATA 帧携带）
  final String? messageId;

  /// 订阅主题（按帧类型要求必填）
  final String? topic;

  /// 序号（可选）
  final int? seq;

  /// 时间戳（毫秒）
  final int timestamp;

  /// 帧载荷（DATA 为业务文本，HELLO 含 sessionId 与版本，ERROR 为错误描述）
  final String? payload;

  /// 创建协议帧
  const PushFrame({
    required this.version,
    required this.type,
    this.messageId,
    this.topic,
    this.seq,
    required this.timestamp,
    this.payload,
  });

  /// 从下行 JSON 反序列化
  ///
  /// @param json 服务端帧对象
  /// @return 协议帧，type 未知时为 null
  static PushFrame? tryFromJson(Map<String, dynamic> json) {
    final typeRaw = json['type'];
    final type = typeRaw is String ? FrameType.tryParseEnumName(typeRaw) : null;
    if (type == null) {
      return null;
    }
    return PushFrame(
      version: (json['version'] as num?)?.toInt() ?? 1,
      type: type,
      messageId: json['messageId'] as String?,
      topic: json['topic'] as String?,
      seq: parseFlexibleInt(json['seq']),
      timestamp: parseFlexibleInt(json['timestamp']) ?? 0,
      payload: json['payload'] as String?,
    );
  }

  /// 序列化为上行 JSON（枚举名大写，对齐服务端 FrameTypeEnum 反序列化）
  ///
  /// @return 上行帧 JSON 对象
  Map<String, dynamic> toJson() => <String, dynamic>{
        'version': version,
        'type': type.wireEnumName,
        if (messageId != null) 'messageId': messageId,
        if (topic != null) 'topic': topic,
        if (seq != null) 'seq': seq,
        'timestamp': timestamp,
        if (payload != null) 'payload': payload,
      };
}

/// SSE 结构化帧上行请求
///
/// 对应服务端 SseUploadForm：POST /private/push/sse/upload 请求体，
/// sessionId 取 hello 帧下发的会话标识。
class FrameUploadRequest {
  /// 会话标识（必填）
  final String sessionId;

  /// 协议帧（必填，type 必填）
  final PushFrame frame;

  /// 创建上行请求
  const FrameUploadRequest({required this.sessionId, required this.frame});

  /// 序列化为请求体
  ///
  /// @return 上行请求 JSON 对象
  Map<String, dynamic> toJson() => <String, dynamic>{
        'sessionId': sessionId,
        'frame': frame.toJson(),
      };

  /// 构造上行订阅帧请求体
  ///
  /// @param sessionId 会话标识
  /// @param version   协议版本
  /// @param topic     订阅主题
  /// @return 上行请求实例
  static FrameUploadRequest subscribe(
    String sessionId,
    int version,
    String topic,
  ) =>
      FrameUploadRequest(
        sessionId: sessionId,
        frame: PushFrame(
          version: version,
          type: FrameType.subscribe,
          topic: topic,
          timestamp: DateTime.now().millisecondsSinceEpoch,
        ),
      );

  /// 构造上行退订帧请求体
  ///
  /// @param sessionId 会话标识
  /// @param version   协议版本
  /// @param topic     退订主题
  /// @return 上行请求实例
  static FrameUploadRequest unsubscribe(
    String sessionId,
    int version,
    String topic,
  ) =>
      FrameUploadRequest(
        sessionId: sessionId,
        frame: PushFrame(
          version: version,
          type: FrameType.unsubscribe,
          topic: topic,
          timestamp: DateTime.now().millisecondsSinceEpoch,
        ),
      );

  /// 构造上行心跳帧请求体
  ///
  /// @param sessionId 会话标识
  /// @param version   协议版本
  /// @return 上行请求实例
  static FrameUploadRequest ping(String sessionId, int version) =>
      FrameUploadRequest(
        sessionId: sessionId,
        frame: PushFrame(
          version: version,
          type: FrameType.ping,
          timestamp: DateTime.now().millisecondsSinceEpoch,
        ),
      );
}
