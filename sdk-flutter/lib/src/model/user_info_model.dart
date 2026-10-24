/// 当前登录用户信息
///
/// 对齐后端 UserInfoVO（GET /private/user/info）。字段名沿用后端原拼写
/// （avaliableRoles/avaliableDepartments）以保证反序列化命中；权限编码取
/// 权限项的 role 字段（对齐前端 getMenuRoleCodes 取 role 的约定）。
library;

import 'json_utils.dart';

/// 用户信息模型
class UserInfoModel {
  /// 用户 id（服务端 Long 序列化为字符串）
  final String? id;

  /// 登录用户名
  final String? userName;

  /// 昵称
  final String? nickName;

  /// 头像地址
  final String? avatar;

  /// 账号状态码
  final int? accountStatus;

  /// 账号状态文案
  final String? accountStatusStr;

  /// 可用角色组列表（含展平前的权限组结构）
  final List<RoleGroupModel> avaliableRoles;

  /// 可用部门列表
  final List<DepartmentModel> avaliableDepartments;

  /// 当前选中部门
  final DepartmentModel? currentDepartment;

  /// 其余未映射字段兜底
  final Map<String, dynamic> extra;

  /// 创建用户信息模型
  const UserInfoModel({
    this.id,
    this.userName,
    this.nickName,
    this.avatar,
    this.accountStatus,
    this.accountStatusStr,
    this.avaliableRoles = const [],
    this.avaliableDepartments = const [],
    this.currentDepartment,
    this.extra = const {},
  });

  /// 从 JSON 反序列化（未承诺字段收进 extra 兜底）
  ///
  /// @param json 服务端用户信息对象
  /// @return 用户信息模型
  static UserInfoModel fromJson(Map<String, dynamic> json) {
    const knownKeys = {
      'id',
      'userName',
      'nickName',
      'avatar',
      'accountStatus',
      'accountStatusStr',
      'avaliableRoles',
      'avaliableDepartments',
      'currentDepartment',
    };
    final extra = <String, dynamic>{
      for (final entry in json.entries)
        if (!knownKeys.contains(entry.key)) entry.key: entry.value,
    };
    return UserInfoModel(
      id: parseFlexibleLong(json['id']),
      userName: json['userName'] as String?,
      nickName: json['nickName'] as String?,
      avatar: json['avatar'] as String?,
      accountStatus: parseFlexibleInt(json['accountStatus']),
      accountStatusStr: json['accountStatusStr'] as String?,
      avaliableRoles: _parseList(json['avaliableRoles'], RoleGroupModel.fromJson),
      avaliableDepartments:
          _parseList(json['avaliableDepartments'], DepartmentModel.fromJson),
      currentDepartment: json['currentDepartment'] is Map
          ? DepartmentModel.fromJson(
              Map<String, dynamic>.from(json['currentDepartment'] as Map))
          : null,
      extra: extra,
    );
  }

  /// 展平全部角色组内的权限编码列表（对齐前端 flatPermissions 逻辑）
  ///
  /// @return 权限编码列表，无角色组时为空列表
  List<String> flattenPermissions() => [
        for (final group in avaliableRoles)
          ...group.permissions
              .where((p) => p.role != null && p.role!.isNotEmpty)
              .map((p) => p.role!),
      ];

  /// 通用列表解析工具
  static List<T> _parseList<T>(
    dynamic raw,
    T Function(Map<String, dynamic>) mapper,
  ) =>
      raw is List
          ? raw
              .whereType<Map<dynamic, dynamic>>()
              .map((e) => mapper(Map<String, dynamic>.from(e)))
              .toList()
          : <T>[];
}

/// 角色组视图（后端 SystemRoleGroupVO：group 为角色组实体，permissions 为权限项列表）
class RoleGroupModel {
  /// 角色组信息实体
  final RoleGroupInfo? group;

  /// 角色组下的权限项列表
  final List<RolePermissionModel> permissions;

  /// 创建角色组视图
  const RoleGroupModel({this.group, this.permissions = const []});

  /// 角色组主键（取自 group.id）
  ///
  /// @return 角色组 id，缺失时为 null
  String? get id => group?.id;

  /// 角色组名称（取自 group.roleGroupName）
  ///
  /// @return 角色组名称，缺失时为 null
  String? get name => group?.roleGroupName;

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端角色组对象
  /// @return 角色组视图
  static RoleGroupModel fromJson(Map<String, dynamic> json) => RoleGroupModel(
        group: json['group'] is Map
            ? RoleGroupInfo.fromJson(Map<String, dynamic>.from(json['group'] as Map))
            : null,
        permissions: UserInfoModel._parseList(
            json['permissions'], RolePermissionModel.fromJson),
      );
}

/// 角色组信息（后端 SystemRoleGroupEntity）
class RoleGroupInfo {
  /// 角色组主键（服务端 Long 序列化为字符串）
  final String? id;

  /// 角色组名称（后端列 role_group_name）
  final String? roleGroupName;

  /// 描述
  final String? description;

  /// 状态标志
  final int? flag;

  /// 创建角色组信息
  const RoleGroupInfo({this.id, this.roleGroupName, this.description, this.flag});

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端角色组实体
  /// @return 角色组信息
  static RoleGroupInfo fromJson(Map<String, dynamic> json) => RoleGroupInfo(
        id: parseFlexibleLong(json['id']),
        roleGroupName: json['roleGroupName'] as String?,
        description: json['description'] as String?,
        flag: parseFlexibleInt(json['flag']),
      );
}

/// 权限项（后端 SystemRoleEntity，权限编码为 role 字段）
class RolePermissionModel {
  /// 权限主键（服务端 Long 序列化为字符串）
  final String? id;

  /// 权限编码（唯一标识，鉴权比对用）
  final String? role;

  /// 权限名称
  final String? name;

  /// 权限类型（1 菜单 / 2 API / 3 数据）
  final int? type;

  /// 描述
  final String? description;

  /// 状态标志
  final int? flag;

  /// 数据范围
  final int? dataScope;

  /// 创建权限项
  const RolePermissionModel({
    this.id,
    this.role,
    this.name,
    this.type,
    this.description,
    this.flag,
    this.dataScope,
  });

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端权限项实体
  /// @return 权限项
  static RolePermissionModel fromJson(Map<String, dynamic> json) =>
      RolePermissionModel(
        id: parseFlexibleLong(json['id']),
        role: json['role'] as String?,
        name: json['name'] as String?,
        type: (json['type'] as num?)?.toInt(),
        description: json['description'] as String?,
        flag: parseFlexibleInt(json['flag']),
        dataScope: parseFlexibleInt(json['dataScope']),
      );
}

/// 部门视图（字段名以后端实际返回为准，主键与名称提供兜底取值）
class DepartmentModel {
  /// 主键（候选，服务端 Long 序列化为字符串）
  final String? id;

  /// 部门 id（候选，服务端 Long 序列化为字符串）
  final String? departmentId;

  /// 部门名称（候选）
  final String? departmentName;

  /// 名称（候选）
  final String? name;

  /// 创建部门视图
  const DepartmentModel({
    this.id,
    this.departmentId,
    this.departmentName,
    this.name,
  });

  /// 取主键（id 与 departmentId 兜底）
  ///
  /// @return 部门主键，均缺失时为 null
  String? get resolvedId => id ?? departmentId;

  /// 取名称（departmentName 与 name 兜底）
  ///
  /// @return 部门名称，均缺失时为 null
  String? get resolvedName => departmentName ?? name;

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端部门对象
  /// @return 部门视图
  static DepartmentModel fromJson(Map<String, dynamic> json) =>
      DepartmentModel(
        id: parseFlexibleLong(json['id']),
        departmentId: parseFlexibleLong(json['departmentId']),
        departmentName: json['departmentName'] as String?,
        name: json['name'] as String?,
      );
}
