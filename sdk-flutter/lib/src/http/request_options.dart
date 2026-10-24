/// 请求级选项
///
/// 对齐前端 app-ui-vue request.js 的请求级控制语义（_skipAuthHandling /
/// _isRefreshRequest / 显式令牌），用于隔离型请求与刷新请求的防死锁标记。
library;

/// 请求级选项
class TbsRequestOptions {
  /// 显式指定令牌（如第三方连通性测试），设置后不再注入当前登录令牌
  final String? explicitToken;

  /// 跳过统一登录态处理（不走刷新/重登/事件通知，直接返回原始响应）
  final bool skipAuthHandling;

  /// 标记本请求为刷新令牌请求自身（刷新失败直接抛出，防止排队死锁）
  final bool isRefreshRequest;

  /// 覆盖全局读取超时（如刷新请求使用短超时）
  final Duration? receiveTimeout;

  /// 追加请求头
  final Map<String, String> headers;

  /// 缺省请求选项
  static const TbsRequestOptions defaults = TbsRequestOptions();

  /// 创建请求级选项
  const TbsRequestOptions({
    this.explicitToken,
    this.skipAuthHandling = false,
    this.isRefreshRequest = false,
    this.receiveTimeout,
    this.headers = const {},
  });
}
