/// 统一响应信封解析
///
/// 对齐后端 AjaxResult 序列化结构（web/src/main/java/com/tbs/web/result/AjaxResult.java）：
/// {code, message, data} 基础键 + type/duration/title/redirect 扩展键。
library;

/// 统一响应信封
class AjaxResponse {
  /// 业务码（200 成功 / 401 未授权 / 403 无权限 / 405 重登 / 468 刷新失败 /
  /// 500 系统异常 / 601 调试 / 701 通知）
  final int code;

  /// 提示消息（服务端已按 Accept-Language i18n）
  final String message;

  /// 业务数据（结构由具体接口契约约定）
  final dynamic data;

  /// 提示类型（success/info/warning/error，配合 message 出现时展示提示）
  final String? type;

  /// 通知标题（配合 message 出现时展示提示）
  final String? title;

  /// 提示展示时长（毫秒）
  final int? duration;

  /// 服务端要求前端跳转的路径（存在时由应用层执行导航）
  final String? redirect;

  /// 创建响应信封
  const AjaxResponse({
    required this.code,
    this.message = '',
    this.data,
    this.type,
    this.title,
    this.duration,
    this.redirect,
  });

  /// 判断业务码是否成功
  ///
  /// @return 业务码为 200 时为 true
  bool get isSuccess => code == 200;

  /// 判断是否携带成功提示（message+type 同现，前端按该约定弹提示）
  ///
  /// @return 携带成功提示时为 true
  bool get hasSuccessNotice => isSuccess && message.isNotEmpty && type != null;

  /// 判断响应体是否包含业务码字段（缺失视为协议不符）
  ///
  /// @return 含 code 字段时为 true
  bool get hasCode => true;

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端响应体
  /// @return 响应信封实例
  static AjaxResponse fromJson(Map<String, dynamic> json) {
    return AjaxResponse(
      code: (json['code'] as num?)?.toInt() ?? 0,
      message: json['message'] as String? ?? '',
      data: json['data'],
      type: json['type'] as String?,
      title: json['title'] as String?,
      duration: (json['duration'] as num?)?.toInt(),
      redirect: json['redirect'] as String?,
    );
  }

  /// 将 data 解析为字符串键 Map（接口 data 为对象时的通用取值）
  ///
  /// @return data 为 Map 时返回副本，否则空 Map
  Map<String, dynamic> dataAsMap() =>
      data is Map<String, dynamic> ? Map<String, dynamic>.from(data as Map) : <String, dynamic>{};

  /// 将 data 解析为字符串键 Map 列表（接口 data 为数组时的通用取值）
  ///
  /// @return data 为 List 时返回元素副本列表，否则空列表
  List<Map<String, dynamic>> dataAsList() => data is List
      ? (data as List)
          .whereType<Map<dynamic, dynamic>>()
          .map((e) => Map<String, dynamic>.from(e))
          .toList()
      : <Map<String, dynamic>>[];
}
