/// 文件流式下载（不支持平台桩实现）
library;

/// 流式下载文件到本地（桩实现：抛出不支持）
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
  throw UnsupportedError('当前平台不支持本地文件下载');
}
