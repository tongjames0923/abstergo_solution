/// dio 底层适配器装配（Web 平台实现）
///
/// 浏览器侧 Cookie 由浏览器自动管理；withCredentials 用于跨域携带凭证
/// （REFRESH_TOKEN HttpOnly Cookie），仅 Web 刷新预设需要。后端 CORS 未开启
/// allowCredentials 时应使用同源反代（对齐 Vue /api 模式）而非跨域直连。
library;

/// 装配 dio 底层适配器（Web 实现：按需开启 withCredentials）
///
/// @param dio             dio 实例
/// @param withCredentials 是否携带跨域凭证
void configureHttpClientAdapter(dynamic dio, {bool withCredentials = false}) {
  if (!withCredentials) {
    return;
  }
  final adapter = dio.httpClientAdapter;
  try {
    adapter.withCredentials = true;
  } on NoSuchMethodError {
    // 非 BrowserHttpClientAdapter 时忽略（如自定义适配器）
  }
}
