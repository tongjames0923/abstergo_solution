import 'dart:convert';

import 'package:crypto/crypto.dart';

import '../constants/tbs_constants.dart';

/// 第三方应用授权码生成
///
/// 对齐后端第三方登录授权码算法与 Java 客户端 ThirdPartyAuthCode：
/// code = 小写十六进制 SHA-256(UTF-8("SALT:appSecret:timestamp:appId"))，
/// 时间窗 ±3 分钟（服务端校验），SALT 与后端固定一致。
class ThirdPartyAuthCode {
  /// 生成第三方应用授权码
  ///
  /// @param appId     应用标识
  /// @param appSecret 应用密钥
  /// @param timestamp 时间戳（毫秒），缺省取当前时间
  /// @return 64 位小写十六进制授权码
  static String generate(
    String appId,
    String appSecret, {
    int? timestamp,
  }) {
    final ts = timestamp ?? DateTime.now().millisecondsSinceEpoch;
    final raw = '$thirdPartyAuthSalt:$appSecret:$ts:$appId';
    final digest = sha256.convert(utf8.encode(raw));
    return digest.toString();
  }

  ThirdPartyAuthCode._();
}
