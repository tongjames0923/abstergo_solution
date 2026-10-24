/// 文件流式下载（Web 平台实现）
///
/// 浏览器沙箱不支持任意路径文件写入，升级安装包下载仅面向原生端；
/// Web 端如需下载由应用层直接打开下载地址交由浏览器处理。
library;

/// 流式下载文件到本地（Web 实现：抛出不支持）
///
/// @param url        下载地址
/// @param savePath   目标文件路径
/// @param onProgress 进度回调
/// @param headers    追加请求头
Future<void> downloadToFile(
  String url,
  String savePath, {
  void Function(int received, int total)? onProgress,
  Map<String, String> headers = const {},
}) async {
  throw UnsupportedError('Web 端不支持本地文件下载，请使用浏览器直接打开下载地址');
}
