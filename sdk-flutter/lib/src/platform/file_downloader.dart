/// 文件流式下载（平台条件导入宿主）
///
/// 下载统一采用"先写 .part 临时文件、成功后原子重命名"策略（对齐 Java 客户端
/// 下载实现，半包文件不会被误用）；Web 端不支持本地文件写入，落桩抛出不支持。
library;

export 'file_downloader_stub.dart'
    if (dart.library.io) 'file_downloader_io.dart'
    if (dart.library.js_interop) 'file_downloader_web.dart';
