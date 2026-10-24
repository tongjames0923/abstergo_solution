/// 令牌存储抽象与内存实现
///
/// 宿主可注入平台实现（移动/桌面安全存储、Web localStorage）持久化令牌；
/// [peek] 供请求头注入同步读取，实现类应自维护内存镜像保证同步读取可行。
library;

/// 令牌存储抽象
abstract class TokenStore {
  /// 同步读取当前令牌（内存缓存语义）
  ///
  /// @return 当前令牌串，未登录时为 null
  String? peek();

  /// 写入令牌（登录/刷新成功后调用）
  ///
  /// @param token 令牌串
  Future<void> write(String token);

  /// 清除令牌（登出/会话失效后调用）
  Future<void> clear();
}

/// 内存令牌存储（默认实现，进程内存活）
class MemoryTokenStore implements TokenStore {
  String? _token;

  @override
  String? peek() => _token;

  @override
  Future<void> write(String token) async {
    _token = token;
  }

  @override
  Future<void> clear() async {
    _token = null;
  }
}
