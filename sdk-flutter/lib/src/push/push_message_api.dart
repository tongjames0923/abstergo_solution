import 'dart:math';

import '../constants/tbs_constants.dart' as c;
import '../exception/tbs_exceptions.dart';
import '../http/base_http_client.dart';
import '../model/push_message_models.dart';

/// 推送消息中心 API
///
/// 对齐 Java 客户端 PushMessageApi 与前端消息中心契约：游标分页拉取、未读数、
/// 批量已读（请求体为裸 id 数组）、通用操作回传与通知发送。
class PushMessageApi {
  final BaseHttpClient _httpClient;

  /// 创建消息中心 API
  ///
  /// @param httpClient 统一 HTTP 客户端
  PushMessageApi({required BaseHttpClient httpClient})
      : _httpClient = httpClient;

  /// 游标分页拉取消息列表
  ///
  /// @param form 查询表单（cursor/limit/readFlag）
  /// @return 消息分页结果
  Future<PushMessagePage> listMessages(PushMessageQueryForm form) async {
    final limit = form.limit == null
        ? null
        : min(form.limit!, c.listPageMax);
    final response = await _httpClient.get(
      c.pathPushList,
      query: PushMessageQueryForm(
        cursor: form.cursor,
        limit: limit,
        readFlag: form.readFlag,
      ).toQuery(),
    );
    return PushMessagePage.fromJson(response.dataAsMap());
  }

  /// 统计未读消息数
  ///
  /// @return 未读数量
  Future<int> unreadCount() async {
    final response = await _httpClient.get(c.pathPushUnreadCount);
    final data = response.data;
    if (data is num) {
      return data.toInt();
    }
    if (data is String) {
      return int.tryParse(data) ?? 0;
    }
    throw const TbsClientException('未读数响应结构异常');
  }

  /// 批量标记已读（请求体为裸 id 数组，单次上限 100）
  ///
  /// @param messageIds 消息 ID 列表
  Future<void> markRead(List<String> messageIds) async {
    if (messageIds.isEmpty) {
      return;
    }
    for (var start = 0; start < messageIds.length; start += c.readBatchMax) {
      final end = min(start + c.readBatchMax, messageIds.length);
      await _httpClient.post(
        c.pathPushRead,
        body: messageIds.sublist(start, end),
      );
    }
  }

  /// 通用操作回传（opType 为除 READ 外的处理行为，如 CLICK）
  ///
  /// @param form 操作回传表单
  Future<void> operate(PushMessageOperateForm form) async {
    await _httpClient.post(c.pathPushOperate, body: form.toJson());
  }

  /// 发送持久化通知
  ///
  /// @param form 发送表单
  /// @return 发送结果
  Future<PushSendResult> notify(PushNotifySendForm form) async {
    final response = await _httpClient.post(c.pathPushNotify, body: form.toJson());
    return PushSendResult.fromJson(response.dataAsMap());
  }
}
