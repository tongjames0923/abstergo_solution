/// HTTP 底层适配器装配（平台条件导入宿主）
///
/// native 平台使用 dio 默认 IOHttpClientAdapter（无需调整）；Web 平台按需
/// 开启 withCredentials 以携带跨域 Cookie（对齐 Vue withCredentials: true），
/// 仅 Web 刷新预设需要，跨域直连时须后端 CORS 开启 allowCredentials。
library;

export 'http_adapter_setup_stub.dart'
    if (dart.library.io) 'http_adapter_setup_io.dart'
    if (dart.library.js_interop) 'http_adapter_setup_web.dart';
