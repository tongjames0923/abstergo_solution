/// 登录令牌模型
///
/// 对应服务端 ITokenModel 序列化结构：不透明令牌串（非 JWT）。令牌类型/状态/
/// 创建方式以原始字符串承载，避免与服务端枚举演进耦合。
class TokenModel {
  /// 令牌串（不透明随机串）
  final String? token;

  /// 登录用户名
  final String? userName;

  /// 令牌类型（WEB_ACCESS_TOKEN/REFRESH_TOKEN/APPLICATION_ACCESS_TOKEN）
  final String? tokenType;

  /// 令牌状态（ACTIVE/EXPIRED/REVOKE 等）
  final String? status;

  /// 会话标识
  final String? sessionId;

  /// 过期时间（ISO8601 或时间戳，由服务端序列化格式决定）
  final String? expireTime;

  /// 创建时间
  final String? createTime;

  /// 创建方式（LOGIN/REFRESH）
  final String? createType;

  /// 创建令牌模型
  const TokenModel({
    this.token,
    this.userName,
    this.tokenType,
    this.status,
    this.sessionId,
    this.expireTime,
    this.createTime,
    this.createType,
  });

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端令牌对象
  /// @return 令牌模型
  static TokenModel fromJson(Map<String, dynamic> json) => TokenModel(
        token: json['token'] as String?,
        userName: json['userName'] as String?,
        tokenType: json['tokenType'] as String?,
        status: json['status'] as String?,
        sessionId: json['sessionId'] as String?,
        expireTime: json['expireTime']?.toString(),
        createTime: json['createTime']?.toString(),
        createType: json['createType'] as String?,
      );
}
