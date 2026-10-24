/// 平台令牌存储条件导入宿主
///
/// Web 使用 localStorage（shared_preferences）；移动/桌面使用安全存储
/// （flutter_secure_storage）。实现均维护内存镜像保证同步读取。
library;

export 'token_store_native.dart'
    if (dart.library.js_interop) 'token_store_web.dart';
