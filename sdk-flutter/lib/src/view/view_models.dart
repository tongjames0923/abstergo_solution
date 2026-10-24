/// 管理端迁移预留：动态视图模型集合
///
/// 对齐前端 app-ui-vue 动态路由契约：GET /private/view/routes 下发路由树
/// （component 字符串映射前端页面），表单配置 GET /private/view/form/{name}。
/// Flutter 端以数据模型承接（导航模型注入 = 管理端迁移入口），页面映射由应用层注册表完成。
library;

import '../model/json_utils.dart';

/// 动态路由节点
///
/// 树节点字段与后端下发一致；component 取值 'Layout'（布局容器）或以 `/` 开头的
/// views 相对路径（如 /admin/third-party/index.vue），Flutter 端由应用层页面注册表映射。
class ViewRouteNode {
  /// 路由路径
  final String? path;

  /// 组件标识（'Layout' 或 views 相对路径）
  final String? component;

  /// 路由名
  final String? name;

  /// 重定向路径
  final String? redirect;

  /// 元信息（title/icon/roles/breadcrumb/activeMenu）
  final ViewRouteMeta? meta;

  /// 是否隐藏
  final bool hidden;

  /// 是否始终显示根节点
  final bool alwaysShow;

  /// 排序号
  final int? sortOrder;

  /// 子节点
  final List<ViewRouteNode> children;

  /// 创建路由节点
  const ViewRouteNode({
    this.path,
    this.component,
    this.name,
    this.redirect,
    this.meta,
    this.hidden = false,
    this.alwaysShow = false,
    this.sortOrder,
    this.children = const [],
  });

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端路由节点
  /// @return 路由节点
  static ViewRouteNode fromJson(Map<String, dynamic> json) => ViewRouteNode(
        path: json['path'] as String?,
        component: json['component'] as String?,
        name: json['name'] as String?,
        redirect: json['redirect'] as String?,
        meta: json['meta'] is Map
            ? ViewRouteMeta.fromJson(
                Map<String, dynamic>.from(json['meta'] as Map))
            : null,
        hidden: json['hidden'] == true,
        alwaysShow: json['alwaysShow'] == true,
        sortOrder: (json['sortOrder'] as num?)?.toInt(),
        children: json['children'] is List
            ? (json['children'] as List)
                .whereType<Map<dynamic, dynamic>>()
                .map((e) =>
                    ViewRouteNode.fromJson(Map<String, dynamic>.from(e)))
                .toList()
            : <ViewRouteNode>[],
      );

  /// 从 JSON 数组反序列化（树根数组）
  ///
  /// @param list 服务端路由树数组
  /// @return 路由节点列表
  static List<ViewRouteNode> listFromJson(List<dynamic> list) => list
      .whereType<Map<dynamic, dynamic>>()
      .map((e) => ViewRouteNode.fromJson(Map<String, dynamic>.from(e)))
      .toList();
}

/// 路由元信息
class ViewRouteMeta {
  /// 展示标题
  final String? title;

  /// 图标标识
  final String? icon;

  /// 可见角色编码（或关系，空表示不限）
  final List<String> roles;

  /// 是否显示面包屑
  final bool breadcrumb;

  /// 高亮菜单路径
  final String? activeMenu;

  /// 创建路由元信息
  const ViewRouteMeta({
    this.title,
    this.icon,
    this.roles = const [],
    this.breadcrumb = true,
    this.activeMenu,
  });

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端 meta 对象
  /// @return 路由元信息
  static ViewRouteMeta fromJson(Map<String, dynamic> json) => ViewRouteMeta(
        title: json['title'] as String?,
        icon: json['icon'] as String?,
        roles: json['roles'] is List
            ? (json['roles'] as List).whereType<String>().toList()
            : <String>[],
        breadcrumb: json['breadcrumb'] != false,
        activeMenu: json['activeMenu'] as String?,
      );
}

/// 动态表单字段配置
class ViewFormField {
  /// 字段标识
  final String prop;

  /// 展示标签
  final String? label;

  /// 字段类型（input/select/date/date-range 等，由 DynamicForm 契约约定）
  final String? type;

  /// 占位文案
  final String? placeholder;

  /// 组件附加属性
  final Map<String, dynamic> props;

  /// 创建表单字段配置
  const ViewFormField({
    required this.prop,
    this.label,
    this.type,
    this.placeholder,
    this.props = const {},
  });

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端字段配置
  /// @return 表单字段配置
  static ViewFormField fromJson(Map<String, dynamic> json) => ViewFormField(
        prop: json['prop'] as String? ?? '',
        label: json['label'] as String?,
        type: json['type'] as String?,
        placeholder: json['placeholder'] as String?,
        props: json['props'] is Map
            ? Map<String, dynamic>.from(json['props'] as Map)
            : <String, dynamic>{},
      );
}

/// 动态表单配置
///
/// 对应 GET /private/view/form/{name} 响应 data。
class ViewFormConfig {
  /// 字段列表
  final List<ViewFormField> fields;

  /// 是否可展开（搜索表单折叠语义）
  final bool expandable;

  /// 栅格跨度
  final int? span;

  /// 标签宽度
  final dynamic labelWidth;

  /// 创建表单配置
  const ViewFormConfig({
    required this.fields,
    this.expandable = false,
    this.span,
    this.labelWidth,
  });

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端表单配置
  /// @return 表单配置
  static ViewFormConfig fromJson(Map<String, dynamic> json) => ViewFormConfig(
        fields: (json['fields'] as List? ?? [])
            .whereType<Map<dynamic, dynamic>>()
            .map((e) => ViewFormField.fromJson(Map<String, dynamic>.from(e)))
            .toList(),
        expandable: json['expandable'] == true,
        span: (json['span'] as num?)?.toInt(),
        labelWidth: json['labelWidth'],
      );
}

/// 路由版本记录
class ViewRouteVersion {
  /// 版本主键（服务端 Long 序列化为字符串）
  final String? id;

  /// 版本号
  final String? versionNo;

  /// 版本描述
  final String? description;

  /// 创建时间
  final String? createTime;

  /// 创建版本记录
  const ViewRouteVersion({
    this.id,
    this.versionNo,
    this.description,
    this.createTime,
  });

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端版本对象
  /// @return 版本记录
  static ViewRouteVersion fromJson(Map<String, dynamic> json) =>
      ViewRouteVersion(
        id: parseFlexibleLong(json['id']),
        versionNo: json['versionNo']?.toString(),
        description: json['description'] as String?,
        createTime: json['createTime']?.toString(),
      );
}
