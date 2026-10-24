/// SSE 下行流传输抽象（平台条件导入宿主）
///
/// native 平台经 package:http IOClient 直连流式读取（读超时无限制，长连接
/// 不被切断）；Web 平台经 fetch_client 走浏览器 fetch 流（支持跨域凭证）。
library;

export 'sse_transport_stub.dart'
    if (dart.library.io) 'sse_transport_io.dart'
    if (dart.library.js_interop) 'sse_transport_web.dart';
