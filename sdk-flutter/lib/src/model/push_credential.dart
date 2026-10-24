/// 推送连接凭证与握手帧载荷模型
library;

/// 推送连接凭证
///
/// 对应服务端 PushCredential：POST /private/push/auth 签发，ticket 为一次性
/// 凭证（TTL 默认 60 秒，禁止复用），建连成功即消费。
class PushCredential {
  /// 一次性连接凭证（UUID）
  final String ticket;

  /// 预分配会话标识（后续上行帧携带）
  final String? sessionId;

  /// 会话属性（认证表单透传，服务端可能追加）
  final Map<String, String> attributes;

  /// 订阅授权集（服务端签票时点计算的可订阅主题模式列表，支持订阅侧通配）
  ///
  /// 不在集内的主题上行订阅会被服务端拒绝（ERROR 帧 code 1008）；
  /// 字段缺失或为空表示服务端未启用订阅鉴权，订阅全放行。
  final List<String>? grantedTopics;

  /// 凭证过期时间
  final String? expireAt;

  /// 创建连接凭证
  const PushCredential({
    required this.ticket,
    this.sessionId,
    this.attributes = const {},
    this.grantedTopics,
    this.expireAt,
  });

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端凭证对象
  /// @return 连接凭证
  static PushCredential fromJson(Map<String, dynamic> json) =>
      PushCredential(
        ticket: json['ticket'] as String? ?? '',
        sessionId: json['sessionId'] as String?,
        attributes: json['attributes'] is Map
            ? Map<String, String>.from(json['attributes'] as Map)
            : <String, String>{},
        grantedTopics: json['grantedTopics'] is List
            ? (json['grantedTopics'] as List).whereType<String>().toList()
            : null,
        expireAt: json['expireAt']?.toString(),
      );
}

/// SSE hello 握手帧载荷
///
/// 对齐后端 hello 帧 payload（需从帧载荷 JSON 二次解析）：sessionId 为后续
/// 上行帧必须携带的会话标识，version 为协商的协议版本。连接以 hello 帧的
/// sessionId 为准，而非 auth 签票时预分配的 sessionId。
class HelloInfo {
  /// 会话标识（握手完成后可用，后续上行帧携带）
  final String sessionId;

  /// 协议版本（可空）
  final int? version;

  /// 创建握手信息
  const HelloInfo({required this.sessionId, this.version});

  /// 从 hello 帧载荷 JSON 反序列化
  ///
  /// @param json hello 帧载荷对象
  /// @return 握手信息，sessionId 缺失时为 null
  static HelloInfo? tryFromJson(Map<String, dynamic> json) {
    final sessionId = json['sessionId'] as String?;
    if (sessionId == null || sessionId.isEmpty) {
      return null;
    }
    return HelloInfo(
      sessionId: sessionId,
      version: (json['version'] as num?)?.toInt(),
    );
  }
}
