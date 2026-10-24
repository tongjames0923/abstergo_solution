import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:tbs_client/tbs_client.dart';

/// 安全存储令牌实现（移动/桌面）
class SecureTokenStore implements TokenStore {
  static const String _key = 'SYSTEM_TOKEN';

  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  String? _mirror;

  @override
  String? peek() => _mirror;

  @override
  Future<void> write(String token) async {
    _mirror = token;
    await _storage.write(key: _key, value: token);
  }

  @override
  Future<void> clear() async {
    _mirror = null;
    await _storage.delete(key: _key);
  }

  /// 启动时恢复内存镜像
  ///
  /// @return 已存储令牌或 null
  Future<String?> restore() async {
    _mirror = await _storage.read(key: _key);
    return _mirror;
  }
}

/// 创建平台令牌存储入口
///
/// @return 令牌存储实例
TokenStore createTokenStore() => SecureTokenStore();
