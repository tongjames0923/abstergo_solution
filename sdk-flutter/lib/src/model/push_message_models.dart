/// 推送消息中心模型集合
library;

import 'json_utils.dart';

/// 推送消息视图
///
/// 对应服务端 PushNotifyMessageVO：messageId 等 Long 字段在 JSON 中为字符串
/// （服务端防雪花 ID 精度丢失），反序列化兼容字符串与数字两种形态。
class PushNotifyMessage {
  /// 消息 ID（雪花 Long，字符串或数字形态）
  final String messageId;

  /// 业务主题
  final String? topic;

  /// 消息类型（notify/warning/reminder，业务可字典驱动扩展）
  final String? msgType;

  /// 标题
  final String? title;

  /// 正文
  final String? content;

  /// 优先级（low/normal/high/urgent）
  final String? priority;

  /// 发送者展示标识
  final String? sender;

  /// 点击跳转地址
  final String? url;

  /// 创建时间
  final String? createTime;

  /// 当前用户是否已读
  final bool readFlag;

  /// 已读时间
  final String? readTime;

  /// 扩展信息
  final String? extraInfo;

  /// 创建推送消息视图
  const PushNotifyMessage({
    required this.messageId,
    this.topic,
    this.msgType,
    this.title,
    this.content,
    this.priority,
    this.sender,
    this.url,
    this.createTime,
    this.readFlag = false,
    this.readTime,
    this.extraInfo,
  });

  /// 解析雪花 Long 字段（兼容字符串与数字形态）
  static String _parseLong(dynamic value) => value?.toString() ?? '';

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端消息对象
  /// @return 推送消息视图
  static PushNotifyMessage fromJson(Map<String, dynamic> json) =>
      PushNotifyMessage(
        messageId: _parseLong(json['messageId']),
        topic: json['topic'] as String?,
        msgType: json['msgType'] as String?,
        title: json['title'] as String?,
        content: json['content'] as String?,
        priority: json['priority'] as String?,
        sender: json['sender'] as String?,
        url: json['url'] as String?,
        createTime: json['createTime']?.toString(),
        readFlag: json['readFlag'] == true,
        readTime: json['readTime']?.toString(),
        extraInfo: json['extraInfo'] as String?,
      );

  /// 从 JSON 数组反序列化
  ///
  /// @param list 服务端消息数组
  /// @return 推送消息视图列表
  static List<PushNotifyMessage> listFromJson(List<dynamic> list) => list
      .whereType<Map<dynamic, dynamic>>()
      .map((e) => PushNotifyMessage.fromJson(Map<String, dynamic>.from(e)))
      .toList();
}

/// 消息分页结果（游标分页）
///
/// 对应服务端 PushMessagePageVO：cursor 为下一页游标（无更多数据时为 null），
/// 调用方以 hasMore 判断是否继续翻页。
class PushMessagePage {
  /// 当前页消息列表
  final List<PushNotifyMessage> list;

  /// 下一页游标（无更多数据时为 null）
  final String? cursor;

  /// 是否还有更多数据
  final bool hasMore;

  /// 创建消息分页结果
  const PushMessagePage({
    required this.list,
    this.cursor,
    this.hasMore = false,
  });

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端分页对象
  /// @return 消息分页结果
  static PushMessagePage fromJson(Map<String, dynamic> json) =>
      PushMessagePage(
        list: PushNotifyMessage.listFromJson(json['list'] as List? ?? []),
        cursor: json['cursor']?.toString(),
        hasMore: json['hasMore'] == true,
      );
}

/// 消息列表查询表单
///
/// 对应服务端 PushMessageQueryForm：游标分页（cursor 为上一页响应的游标），
/// 到达序倒序、含已读。
class PushMessageQueryForm {
  /// 翻页游标（首页不传）
  final String? cursor;

  /// 单页条数（缺省 20，上限 100）
  final int? limit;

  /// 已读筛选（all/unread，缺省 all）
  final String? readFlag;

  /// 创建查询表单
  const PushMessageQueryForm({this.cursor, this.limit, this.readFlag});

  /// 序列化为查询参数
  ///
  /// @return 查询参数对象
  Map<String, dynamic> toQuery() => <String, dynamic>{
        if (cursor != null && cursor!.isNotEmpty) 'cursor': cursor,
        if (limit != null) 'limit': limit,
        if (readFlag != null) 'readFlag': readFlag,
      };
}

/// 推送通知发送表单
///
/// 对应服务端 PushNotifySendForm 契约：msgType 必填，接收人不随表单传递
/// （由服务端 SPI 解析），source 缺省由服务端按 http 通道处理。
class PushNotifySendForm {
  /// 消息类型（必填，业务可字典驱动扩展）
  final String msgType;

  /// 标题（可选，不超过 255 字符）
  final String? title;

  /// 正文（可选）
  final String? content;

  /// 优先级（可选白名单：low/normal/high/urgent，缺省 normal）
  final String? priority;

  /// 业务主题（可选，不超过 128 字符，缺省 global.broadcast）
  final String? topic;

  /// 发送者展示标识（可选，HTTP 场景缺省取当前登录用户）
  final String? sender;

  /// 点击跳转地址（可选，不超过 512 字符）
  final String? url;

  /// 业务扩展字段（可选，原样序列化为 ext_info JSON）
  final Object? extInfo;

  /// 来源通道（可选：http/rpc/job/system，缺省 http）
  final String? source;

  /// 创建发送表单
  const PushNotifySendForm({
    required this.msgType,
    this.title,
    this.content,
    this.priority,
    this.topic,
    this.sender,
    this.url,
    this.extInfo,
    this.source,
  });

  /// 序列化为请求体
  ///
  /// @return 发送表单 JSON 对象
  Map<String, dynamic> toJson() => <String, dynamic>{
        'msgType': msgType,
        if (title != null) 'title': title,
        if (content != null) 'content': content,
        if (priority != null) 'priority': priority,
        if (topic != null) 'topic': topic,
        if (sender != null) 'sender': sender,
        if (url != null) 'url': url,
        if (extInfo != null) 'extInfo': extInfo,
        if (source != null) 'source': source,
      };
}

/// 推送发送结果视图
///
/// 对应服务端 PushSendResultVO：接收人数为真实送达回执依据，
/// 信令数为信令通道发布数量（tbs.push.enable 关闭时为 0）。
class PushSendResult {
  /// 消息 ID
  final String messageId;

  /// 业务主题
  final String? topic;

  /// 消息类型
  final String? msgType;

  /// 接收人数
  final int? receiverCount;

  /// 信令发布数量
  final int? signalCount;

  /// 创建发送结果
  const PushSendResult({
    required this.messageId,
    this.topic,
    this.msgType,
    this.receiverCount,
    this.signalCount,
  });

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端发送结果对象
  /// @return 发送结果
  static PushSendResult fromJson(Map<String, dynamic> json) => PushSendResult(
        messageId: json['messageId']?.toString() ?? '',
        topic: json['topic'] as String?,
        msgType: json['msgType'] as String?,
        receiverCount: parseFlexibleInt(json['receiverCount']),
        signalCount: parseFlexibleInt(json['signalCount']),
      );
}

/// 消息操作回传表单
///
/// 对应服务端 PushMessageOperateForm：messageId 与 opType 必填，
/// opType 为除 READ 外的处理行为（如 CLICK，服务端对 CLICK 默认补记 READ）。
class PushMessageOperateForm {
  /// 消息 ID（必填）
  final String messageId;

  /// 操作类型（必填，如 CLICK）
  final String opType;

  /// 扩展信息（可选）
  final String? extInfo;

  /// 创建操作回传表单
  const PushMessageOperateForm({
    required this.messageId,
    required this.opType,
    this.extInfo,
  });

  /// 序列化为请求体
  ///
  /// @return 操作回传 JSON 对象
  Map<String, dynamic> toJson() => <String, dynamic>{
        'messageId': messageId,
        'opType': opType,
        if (extInfo != null) 'extInfo': extInfo,
      };
}
