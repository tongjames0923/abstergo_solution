/// 客户端事件流模型
///
/// HTTP 层在统一响应处理过程中发出的横切事件（会话失效/提示/跳转），
/// 宿主订阅后执行引导重登、Toast 展示、路由跳转等应用级行为。
library;

/// 客户端事件基类
abstract class TbsClientEvent {
  /// 创建事件
  const TbsClientEvent();
}

/// 会话失效事件（业务码命中 SessionPolicy 重登策略时触发，宿主据此引导重登）
class SessionExpiredEvent extends TbsClientEvent {
  /// 触发失效的业务码（401/405/468）
  final int code;

  /// 服务端提示消息
  final String message;

  /// 创建会话失效事件
  const SessionExpiredEvent({required this.code, required this.message});
}

/// 通知事件（成功响应携带 message+type、或业务码 701 时触发，宿主展示 Toast/通知）
class NoticeEvent extends TbsClientEvent {
  /// 通知内容
  final String message;

  /// 通知类型（success/info/warning/error）
  final String? type;

  /// 通知标题
  final String? title;

  /// 展示时长（毫秒）
  final int? duration;

  /// 创建通知事件
  const NoticeEvent({
    required this.message,
    this.type,
    this.title,
    this.duration,
  });
}

/// 跳转事件（响应携带 redirect 字段时触发，宿主据此执行路由跳转）
class RedirectEvent extends TbsClientEvent {
  /// 目标路径
  final String path;

  /// 创建跳转事件
  const RedirectEvent({required this.path});
}
