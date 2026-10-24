/// 文件流式下载（native 平台实现）
///
/// 基于 package:http 流式落盘：先写 `.part` 临时文件，全部字节落盘成功后
/// 原子重命名为目标文件，进程中断不会留下可被误用的半包文件。
library;

import 'dart:io';

import 'package:http/http.dart' as http;

import '../exception/tbs_exceptions.dart';

/// 流式下载文件到本地（native 实现）
///
/// @param url        下载地址（完整 URL）
/// @param savePath   目标文件路径
/// @param onProgress 进度回调（received/total 字节数，total 未知时为 -1）
/// @param headers    追加请求头
Future<void> downloadToFile(
  String url,
  String savePath, {
  void Function(int received, int total)? onProgress,
  Map<String, String> headers = const {},
}) async {
  final client = http.Client();
  try {
    final request = http.Request('GET', Uri.parse(url))
      ..headers.addAll(headers);
    final response = await client.send(request);
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw TbsClientException('下载失败（HTTP ${response.statusCode}）');
    }
    final partFile = File('$savePath.part');
    final sink = partFile.openWrite();
    final total = response.contentLength ?? -1;
    var received = 0;
    try {
      await for (final chunk in response.stream) {
        received += chunk.length;
        sink.add(chunk);
        onProgress?.call(received, total);
      }
      await sink.flush();
      await sink.close();
      await partFile.rename(savePath);
    } catch (error) {
      try {
        await sink.close();
      } catch (_) {
        // 关闭失败不掩盖原始错误
      }
      if (await partFile.exists()) {
        await partFile.delete();
      }
      if (error is TbsException) {
        rethrow;
      }
      throw TbsClientException('下载写入失败', cause: error);
    }
  } on TbsException {
    rethrow;
  } catch (error) {
    throw TbsClientException('下载请求失败', cause: error);
  } finally {
    client.close();
  }
}
