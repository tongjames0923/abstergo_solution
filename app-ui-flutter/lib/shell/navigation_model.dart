import 'package:flutter/material.dart';

import '../shared/l10n/app_localizations.dart';

/// 导航项模型
///
/// 外壳导航的数据源：静态三入口；管理端迁移时可将后端动态路由树
/// （ViewApi.fetchRoutes，菜单权限 type=1）映射为 [AppNavItem] 列表注入，
/// 外壳渲染逻辑无需变更。
class AppNavItem {
  /// 路由路径
  final String route;

  /// 图标构造器
  final IconData Function(bool selected) icon;

  /// 标签构造器
  final String Function(AppLocalizations l10n) label;

  /// 创建导航项
  ///
  /// @param route 路由路径
  /// @param icon  图标构造器
  /// @param label 标签构造器
  const AppNavItem({
    required this.route,
    required this.icon,
    required this.label,
  });

  /// 缺省导航项（首页/消息/我的）
  ///
  /// @return 导航项列表
  static List<AppNavItem> defaults() => [
        AppNavItem(
          route: '/home',
          icon: (selected) =>
              selected ? Icons.home_rounded : Icons.home_outlined,
          label: (l10n) => l10n.navHome,
        ),
        AppNavItem(
          route: '/messages',
          icon: (selected) =>
              selected ? Icons.inbox_rounded : Icons.inbox_outlined,
          label: (l10n) => l10n.navMessages,
        ),
        AppNavItem(
          route: '/profile',
          icon: (selected) =>
              selected ? Icons.person_rounded : Icons.person_outline_rounded,
          label: (l10n) => l10n.navProfile,
        ),
      ];

}
