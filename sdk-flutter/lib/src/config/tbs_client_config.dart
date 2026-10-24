import 'push_options.dart';
import 'session_policy.dart';

/// 客户端全局配置（不可变对象）
///
/// @param baseUrl        服务端基础地址（必填，如 https://api.example.com）
/// @param tokenHeaderName 令牌请求头字段名（需与服务端令牌拾取配置一致）
/// @param language       Accept-Language 请求头取值（zh_CN / en_US）
/// @param connectTimeout 连接超时
/// @param receiveTimeout 读取超时
/// @param sendTimeout    写入超时
/// @param sessionPolicy  错误码到会话行为的映射策略
/// @param pushOptions    SSE 推送参数
class TbsClientConfig {
  /// 服务端基础地址
  final String baseUrl;

  /// 令牌请求头字段名
  final String tokenHeaderName;

  /// 语言头取值
  final String language;

  /// 连接超时
  final Duration connectTimeout;

  /// 读取超时
  final Duration receiveTimeout;

  /// 写入超时
  final Duration sendTimeout;

  /// 会话策略
  final SessionPolicy sessionPolicy;

  /// SSE 推送参数
  final PushOptions pushOptions;

  /// 创建客户端配置
  const TbsClientConfig({
    required this.baseUrl,
    this.tokenHeaderName = 'Authorization',
    this.language = 'zh_CN',
    this.connectTimeout = const Duration(seconds: 10),
    this.receiveTimeout = const Duration(seconds: 30),
    this.sendTimeout = const Duration(seconds: 30),
    this.sessionPolicy = SessionPolicy.native,
    this.pushOptions = const PushOptions(),
  });

  /// 原生端默认配置（重登策略，不做令牌刷新）
  ///
  /// @param baseUrl 服务端基础地址
  /// @return 预填原生会话策略的配置
  static TbsClientConfig forNative(String baseUrl) => TbsClientConfig(
        baseUrl: baseUrl,
        sessionPolicy: SessionPolicy.native,
      );

  /// Web 端默认配置（启用 Cookie 刷新流，需后端支持跨域凭证）
  ///
  /// @param baseUrl 服务端基础地址
  /// @return 预填 Web 会话策略的配置
  static TbsClientConfig forWeb(String baseUrl) => TbsClientConfig(
        baseUrl: baseUrl,
        sessionPolicy: SessionPolicy.web,
      );
}
