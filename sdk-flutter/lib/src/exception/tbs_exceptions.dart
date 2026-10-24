/// SDK 异常体系
///
/// 对齐仓库 Java 客户端（com.tbs.sdk.exception）的异常家族语义：
/// 服务端统一响应业务码映射为强类型异常，调用方按异常类型分层处置。
library;

/// SDK 异常基类
///
/// @param message 错误描述（服务端 i18n 结果或本地说明）
/// @param code    服务端统一响应业务码（网络类异常为 null）
/// @param cause   原始异常（便于排障，可选）
class TbsException implements Exception {
  /// 错误描述
  final String message;

  /// 服务端统一响应业务码
  final int? code;

  /// 原始异常
  final Object? cause;

  /// 创建 SDK 异常
  const TbsException(this.message, {this.code, this.cause});

  @override
  String toString() => 'TbsException(code: $code, message: $message)';
}

/// 认证异常（业务码 401 未授权）
class TbsAuthException extends TbsException {
  /// 创建认证异常
  const TbsAuthException(super.message, {super.code, super.cause});

  @override
  String toString() => 'TbsAuthException(code: $code, message: $message)';
}

/// 需要重新登录异常（业务码 405/468，宿主应引导用户重新登录）
///
/// 继承 [TbsAuthException]，捕获父类即可同时覆盖 401 与 405/468 场景。
class TbsReloginRequiredException extends TbsAuthException {
  /// 创建重登异常
  const TbsReloginRequiredException(super.message, {super.code, super.cause});

  @override
  String toString() =>
      'TbsReloginRequiredException(code: $code, message: $message)';
}

/// 服务端异常（业务码 403/500/601/701 等其他非成功码）
class TbsServerException extends TbsException {
  /// 创建服务端异常
  const TbsServerException(super.message, {super.code, super.cause});

  @override
  String toString() => 'TbsServerException(code: $code, message: $message)';
}

/// 客户端异常（网络/超时/解析失败/参数非法，可重试或本地排障）
class TbsClientException extends TbsException {
  /// 创建客户端异常
  const TbsClientException(super.message, {super.code, super.cause});

  @override
  String toString() => 'TbsClientException(code: $code, message: $message)';
}
