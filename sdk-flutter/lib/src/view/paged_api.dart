/// 管理端迁移预留：泛化分页查询协议
///
/// 对齐前端管理端表格查询契约（QuickUseManagementView 等）：
/// `POST {url}?pageNum=&pageSize=`，条件与排序放 body。条件字段命名约定：
/// 精确 `xxxEquals`、模糊 `xxxLike`、IN `xxxIn`/`xxxList`（值为数组）、
/// 时间范围 `createTimeGreaterThan`/`createTimeLessThan`、排序 `{field}Order`
/// （值为字符串 'ASC'|'DESC'）。响应信封 `data = {data: T[], total: n}`。
library;

import '../model/json_utils.dart';

import '../http/base_http_client.dart';

/// 通用分页查询请求
class PageQuery {
  /// 页码（从 1 计）
  final int pageNum;

  /// 页大小
  final int pageSize;

  /// 创建分页查询请求
  ///
  /// @param pageNum  页码
  /// @param pageSize 页大小
  const PageQuery({required this.pageNum, required this.pageSize});

  /// 序列化为 query 参数
  ///
  /// @return query 参数对象
  Map<String, dynamic> toQuery() =>
      <String, dynamic>{'pageNum': pageNum, 'pageSize': pageSize};
}

/// 通用分页查询结果
///
/// @param T 列表元素类型
class PageResult<T> {
  /// 当前页数据
  final List<T> data;

  /// 总条数
  final int total;

  /// 创建分页结果
  const PageResult({required this.data, required this.total});

  /// 从信封 data 解析（元素由 mapper 转换）
  ///
  /// @param json   信封 data 对象（{data, total}）
  /// @param mapper 元素反序列化函数
  /// @return 分页结果
  static PageResult<T> fromJson<T>(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) mapper,
  ) =>
      PageResult<T>(
        data: (json['data'] as List? ?? [])
            .whereType<Map<dynamic, dynamic>>()
            .map((e) => mapper(Map<String, dynamic>.from(e)))
            .toList(),
        total: parseFlexibleInt(json['total']) ?? 0,
      );
}

/// 泛化分页查询 API（管理端页面迁移的协议底座）
///
/// 条件对象不做强类型建模（各业务域字段约定不同），由调用方按命名约定组装；
/// 常用条件构造器见 [PagedConditions]。
class PagedApi {
  final BaseHttpClient _httpClient;

  /// 创建泛化分页 API
  ///
  /// @param httpClient 统一 HTTP 客户端
  PagedApi({required BaseHttpClient httpClient}) : _httpClient = httpClient;

  /// 泛化分页查询
  ///
  /// @param url        查询端点（如 /private/admin/user/query）
  /// @param page       分页参数
  /// @param conditions 条件对象（按后端命名约定组装，可含排序字段）
  /// @return 分页结果（元素为原始 JSON Map）
  Future<PageResult<Map<String, dynamic>>> query(
    String url,
    PageQuery page,
    Map<String, dynamic> conditions,
  ) async {
    final response = await _httpClient.post(
      url,
      body: conditions,
      query: page.toQuery(),
    );
    return PageResult.fromJson<Map<String, dynamic>>(
      response.dataAsMap(),
      (e) => e,
    );
  }

  /// 泛化单条查询
  ///
  /// @param url 详情端点模板（如 /private/admin/user/get/{id}）
  /// @return 原始 JSON Map
  Future<Map<String, dynamic>> get(String url) async {
    final response = await _httpClient.get(url);
    return response.dataAsMap();
  }

  /// 泛化保存/提交
  ///
  /// @param url  保存端点
  /// @param body 表单对象
  /// @return 响应信封 data（结构由具体接口决定）
  Future<dynamic> save(String url, Map<String, dynamic> body) async {
    final response = await _httpClient.post(url, body: body);
    return response.data;
  }

  /// 泛化删除
  ///
  /// @param url 删除端点模板（如 /private/admin/user/delete/{id}）
  Future<void> delete(String url) async {
    await _httpClient.delete(url);
  }
}

/// 常用查询条件构造器（对齐后端 QueryEngine 条件命名约定）
class PagedConditions {
  /// 精确匹配条件
  ///
  /// @param field 字段名
  /// @param value 精确值
  /// @return 条件键值对
  static Map<String, dynamic> equals(String field, Object value) =>
      {'${field}Equals': value};

  /// 模糊匹配条件
  ///
  /// @param field 字段名
  /// @param value 模糊值
  /// @return 条件键值对
  static Map<String, dynamic> like(String field, String value) =>
      {'${field}Like': value};

  /// IN 条件
  ///
  /// @param field  字段名
  /// @param values 值列表
  /// @return 条件键值对
  static Map<String, dynamic> inList(String field, List<Object> values) =>
      {'${field}In': values};

  /// 排序条件
  ///
  /// @param field     排序字段名
  /// @param ascending 是否升序
  /// @return 条件键值对
  static Map<String, dynamic> orderBy(String field, bool ascending) =>
      {'${field}Order': ascending ? 'ASC' : 'DESC'};

  PagedConditions._();
}
