import 'package:flutter/material.dart';
import 'package:forui/forui.dart';
import 'package:go_router/go_router.dart';

import '../shared/l10n/app_localizations.dart';
import 'app_top_bar.dart';
import 'navigation_model.dart';

/// 自适应外壳
///
/// 宽屏（>=720 逻辑像素）：左侧导航栏 + 顶栏 + 内容区，承接管理端迁移；
/// 窄屏（移动端）：顶栏 + 内容区 + 底部导航，单栏卡片流。
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
    final wide = MediaQuery.sizeOf(context).width >= 720;
    final title = items[currentIndex].label(l10n);
    if (wide) {
      return Scaffold(
        body: Row(
          children: [
            _SideRail(
              items: items,
              currentIndex: currentIndex,
              onSelect: _goBranch,
            ),
            Expanded(
              child: Column(
                children: [
                  AppTopBar(title: title),
                  Expanded(child: navigationShell),
                ],
              ),
            ),
          ],
        ),
      );
    }
    return Scaffold(
      body: Column(
        children: [
          AppTopBar(title: title),
          Expanded(child: navigationShell),
        ],
      ),
      bottomNavigationBar: _BottomNav(
        items: items,
        currentIndex: currentIndex,
        onSelect: _goBranch,
      ),
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

class _SideRail extends StatelessWidget {
  final List<AppNavItem> items;

  final int currentIndex;

  final ValueChanged<int> onSelect;

  const _SideRail({
    required this.items,
    required this.currentIndex,
    required this.onSelect,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 224,
      decoration: BoxDecoration(
        color: context.theme.colors.card,
        border: Border(right: BorderSide(color: context.theme.colors.border)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 20, 16, 12),
            child: Row(
              children: [
                Container(
                  width: 32,
                  height: 32,
                  decoration: BoxDecoration(
                    color: context.theme.colors.primary,
                    borderRadius: BorderRadius.circular(10),
                  ),
                  alignment: Alignment.center,
                  child: Icon(
                    Icons.bolt_rounded,
                    size: 20,
                    color: context.theme.colors.primaryForeground,
                  ),
                ),
                const SizedBox(width: 10),
                Text(
                  'TBS',
                  style: context.theme.typography.body.md.copyWith(
                    fontWeight: FontWeight.w700,
                    letterSpacing: 1.2,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 8),
          for (var i = 0; i < items.length; i++)
            _RailItem(
              item: items[i],
              selected: i == currentIndex,
              onSelect: () => onSelect(i),
            ),
          const Spacer(),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              'v1.0.0',
              style: TextStyle(
                fontSize: 11,
                color: context.theme.colors.mutedForeground,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _RailItem extends StatelessWidget {
  final AppNavItem item;

  final bool selected;

  final VoidCallback onSelect;

  const _RailItem({
    required this.item,
    required this.selected,
    required this.onSelect,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final fg = selected
        ? context.theme.colors.primary
        : context.theme.colors.mutedForeground;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 2),
      child: Material(
        color: selected ? context.theme.colors.secondary : Colors.transparent,
        borderRadius: BorderRadius.circular(10),
        child: InkWell(
          borderRadius: BorderRadius.circular(10),
          onTap: onSelect,
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
            child: Row(
              children: [
                Icon(item.icon(selected), size: 20, color: fg),
                const SizedBox(width: 10),
                Text(
                  item.label(l10n),
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: selected ? FontWeight.w600 : FontWeight.w400,
                    color: fg,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _BottomNav extends StatelessWidget {
  final List<AppNavItem> items;

  final int currentIndex;

  final ValueChanged<int> onSelect;

  const _BottomNav({
    required this.items,
    required this.currentIndex,
    required this.onSelect,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: context.theme.colors.card,
        border: Border(top: BorderSide(color: context.theme.colors.border)),
      ),
      child: SafeArea(
        child: Row(
          children: [
            for (var i = 0; i < items.length; i++)
              Expanded(
                child: _BottomNavItem(
                  item: items[i],
                  selected: i == currentIndex,
                  onSelect: () => onSelect(i),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _BottomNavItem extends StatelessWidget {
  final AppNavItem item;

  final bool selected;

  final VoidCallback onSelect;

  const _BottomNavItem({
    required this.item,
    required this.selected,
    required this.onSelect,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final fg = selected
        ? context.theme.colors.primary
        : context.theme.colors.mutedForeground;
    return InkWell(
      onTap: onSelect,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 10),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(item.icon(selected), size: 22, color: fg),
            const SizedBox(height: 4),
            Text(
              item.label(l10n),
              style: TextStyle(
                fontSize: 11,
                fontWeight: selected ? FontWeight.w600 : FontWeight.w400,
                color: fg,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
