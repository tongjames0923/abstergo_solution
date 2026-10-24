import '../constants/tbs_constants.dart';
import '../exception/tbs_exceptions.dart';
import '../http/base_http_client.dart';
import 'view_models.dart';

/// 动态视图 API（管理端迁移预留）
///
/// 对齐前端 app-ui-vue view.js 契约：动态路由树下发、表单配置、路由分组与版本。
/// 管理端迁移时由应用层把 [fetchRoutes] 结果注入外壳导航模型即可承接菜单。
class ViewApi {
  final BaseHttpClient _httpClient;

  /// 创建动态视图 API
  ///
  /// @param httpClient 统一 HTTP 客户端
  ViewApi({required BaseHttpClient httpClient}) : _httpClient = httpClient;

  /// 拉取当前用户可见的动态路由树
  ///
  /// @return 路由节点树（顶层为数组）
  Future<List<ViewRouteNode>> fetchRoutes() async {
    final response = await _httpClient.get(pathViewRoutes);
    return ViewRouteNode.listFromJson(response.dataAsList());
  }

  /// 拉取动态表单配置
  ///
  /// @param name 配置名（如 userAddForm）
  /// @return 表单配置
  Future<ViewFormConfig> fetchForm(String name) async {
    final response = await _httpClient.get('$pathViewForm$name');
    final data = response.data;
    if (data is! Map) {
      throw const TbsClientException('表单配置响应结构异常');
    }
    return ViewFormConfig.fromJson(Map<String, dynamic>.from(data));
  }

  /// 拉取路由分组列表
  ///
  /// @return 分组名字符串数组
  Future<List<String>> listRouteGroups() async {
    final response = await _httpClient.get('/private/view/route-groups');
    return response.dataAsList().map((e) => e.toString()).toList();
  }

  /// 按分组拉取路由树（含 sortOrder，服务端已排序）
  ///
  /// @param group 分组名
  /// @return 路由节点树
  Future<List<ViewRouteNode>> fetchRoutesByGroup(String group) async {
    final response = await _httpClient.get('$pathViewRoutes/$group');
    return ViewRouteNode.listFromJson(response.dataAsList());
  }

  /// 拉取分组版本列表
  ///
  /// @param group 分组名
  /// @return 版本记录列表
  Future<List<ViewRouteVersion>> listVersions(String group) async {
    final response = await _httpClient.get('$pathViewRoutes/$group/versions');
    return response
        .dataAsList()
        .map((e) => ViewRouteVersion.fromJson(e))
        .toList();
  }
}
