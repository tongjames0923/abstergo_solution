/// SSE 下行流传输抽象（平台条件导入宿主）
///
/// native 平台经 package:http IOClient 直连流式读取（读超时无限制，长连接
/// 不被切断）；Web 平台经 fetch_client 走浏览器 fetch 流。凭证模式跟随
/// 会话策略：仅 Web Cookie 刷新预设携带跨域凭证（credentials: include），
/// 其余场景 same-origin，避免后端 CORS `*` 通配与凭证互斥被浏览器拦截。
library;

export 'sse_transport_stub.dart'
    if (dart.library.io) 'sse_transport_io.dart'
    if (dart.library.js_interop) 'sse_transport_web.dart';
