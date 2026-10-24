import 'package:shared_preferences/shared_preferences.dart';
import 'package:tbs_client/tbs_client.dart';

/// 浏览器 localStorage 令牌实现（Web，经 shared_preferences）
class LocalStorageTokenStore implements TokenStore {
  static const String _key = 'SYSTEM_TOKEN';

  String? _mirror;

  @override
  String? peek() => _mirror;

  @override
  Future<void> write(String token) async {
    _mirror = token;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_key, token);
  }

  @override
  Future<void> clear() async {
    _mirror = null;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_key);
  }

  /// 启动时恢复内存镜像
  ///
  /// @return 已存储令牌或 null
  Future<String?> restore() async {
    final prefs = await SharedPreferences.getInstance();
    _mirror = prefs.getString(_key);
    return _mirror;
  }
}

/// 创建平台令牌存储入口
///
/// @return 令牌存储实例
TokenStore createTokenStore() => LocalStorageTokenStore();
