/// 登录方式元数据
///
/// 对应 GET /public/user/login-grants 的数组项，描述服务端启用的授权方式及其公开配置。
class LoginGrantModel {
  /// 授权类型标识（如 password/wecom/third_party）
  final String grantType;

  /// 展示文案 i18n key
  final String labelKey;

  /// 公开配置（如企微 corpId/agentId，键值结构由服务端约定）
  final Map<String, dynamic> publicOptions;

  /// 创建登录方式元数据
  const LoginGrantModel({
    required this.grantType,
    required this.labelKey,
    this.publicOptions = const {},
  });

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端授权方式对象
  /// @return 登录方式元数据
  static LoginGrantModel fromJson(Map<String, dynamic> json) => LoginGrantModel(
        grantType: json['grantType'] as String? ?? '',
        labelKey: json['labelKey'] as String? ?? '',
        publicOptions: json['publicOptions'] is Map
            ? Map<String, dynamic>.from(json['publicOptions'] as Map)
            : <String, dynamic>{},
      );

  /// 从 JSON 数组反序列化
  ///
  /// @param list 服务端授权方式数组
  /// @return 登录方式元数据列表
  static List<LoginGrantModel> listFromJson(List<dynamic> list) => list
      .whereType<Map<dynamic, dynamic>>()
      .map((e) => LoginGrantModel.fromJson(Map<String, dynamic>.from(e)))
      .toList();
}
