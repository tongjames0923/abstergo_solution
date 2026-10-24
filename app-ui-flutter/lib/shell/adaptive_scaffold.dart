import 'package:flutter/cupertino.dart';
import 'package:go_router/go_router.dart';

import '../shared/design/app_theme.dart';
import '../shared/l10n/app_localizations.dart';
import 'navigation_model.dart';

/// 自适应外壳
///
/// 窄屏（<900）：CupertinoTabScaffold 底部 Tab 栏，页面各自承载 Large Title 导航栏；
/// 宽屏（>=900）：浮岛侧栏 + 内容区，承接管理端迁移。
class AdaptiveScaffold extends StatelessWidget {
  /// 分支导航壳
  final StatefulNavigationShell navigationShell;

  /// 创建自适应外壳
  ///
  /// @param navigationShell 分支导航壳
  const AdaptiveScaffold({super.key, required this.navigationShell});

  @override
  Widget build(BuildContext context) {
    final items = AppNavItem.defaults();
    final l10n = AppLocalizations.of(context);
    final currentIndex = _resolveIndex(items);
    final wide = MediaQuery.sizeOf(context).width >= 900;
    if (wide) {
      return Row(
        children: [
          SizedBox(
            width: 260,
            child: _Sidebar(
              items: items,
              currentIndex: currentIndex,
              onSelect: _goBranch,
            ),
          ),
          Expanded(child: navigationShell),
        ],
      );
    }
    return CupertinoTabScaffold(
      tabBar: CupertinoTabBar(
        currentIndex: currentIndex,
        onTap: _goBranch,
        items: [
          for (var i = 0; i < items.length; i++)
            BottomNavigationBarItem(
              icon: Icon(items[i].icon(i == currentIndex)),
              label: items[i].label(l10n),
            ),
        ],
      ),
      tabBuilder: (context, index) => navigationShell,
    );
  }

  /// 当前分支在导航项中的下标
  int _resolveIndex(List<AppNavItem> items) {
    final location = navigationShell.shellRouteContext.routerState.uri.path;
    for (var i = 0; i < items.length; i++) {
      if (location.startsWith(items[i].route)) {
        return i;
      }
    }
    return 0;
  }

  /// 切换分支（回到分支初始页）
  ///
  /// @param index 目标分支下标
  void _goBranch(int index) {
    navigationShell.goBranch(
      index,
      initialLocation: index == navigationShell.currentIndex,
    );
  }
}

/// 宽屏侧栏：分组背景 + 发丝分割线
class _Sidebar extends StatelessWidget {
  final List<AppNavItem> items;

  final int currentIndex;

  final ValueChanged<int> onSelect;

  const _Sidebar({
    required this.items,
    required this.currentIndex,
    required this.onSelect,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final theme = CupertinoTheme.of(context);
    return ColoredBox(
      color: AppTheme.secondaryBackground(context),
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Padding(
                padding: const EdgeInsets.fromLTRB(8, 8, 8, 16),
                child: Row(
                  children: [
                    Icon(CupertinoIcons.bolt_fill,
                        size: 22, color: theme.primaryColor),
                    const SizedBox(width: 8),
                    Text(
                      'TBS',
                      style: theme.textTheme.navTitleTextStyle
                          .copyWith(fontSize: 17),
                    ),
                  ],
                ),
              ),
              for (var i = 0; i < items.length; i++)
                Padding(
                  padding: const EdgeInsets.only(bottom: 4),
                  child: CupertinoButton(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 12,
                    ),
                    color: i == currentIndex
                        ? theme.primaryColor.withValues(alpha: 0.12)
                        : CupertinoColors.transparent,
                    borderRadius: BorderRadius.circular(10),
                    onPressed: () => onSelect(i),
                    child: Row(
                      children: [
                        Icon(
                          items[i].icon(i == currentIndex),
                          size: 20,
                          color: i == currentIndex
                              ? theme.primaryColor
                              : AppTheme.secondaryLabel(context),
                        ),
                        const SizedBox(width: 10),
                        Text(
                          items[i].label(l10n),
                          style: TextStyle(
                            fontSize: 15,
                            fontWeight: i == currentIndex
                                ? FontWeight.w600
                                : FontWeight.w400,
                            color: i == currentIndex
                                ? theme.primaryColor
                                : CupertinoTheme.of(context)
                                    .textTheme
                                    .textStyle
                                    .color,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              const Spacer(),
              Padding(
                padding: const EdgeInsets.fromLTRB(8, 0, 8, 8),
                child: Text(
                  'v1.0.0',
                  style: TextStyle(
                    fontSize: 11,
                    color: AppTheme.secondaryLabel(context),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
