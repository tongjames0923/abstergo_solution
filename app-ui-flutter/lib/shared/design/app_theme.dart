import 'package:flutter/cupertino.dart';

/// 设计令牌：靛蓝主色 + Cupertino 主题
class AppTheme {
  /// 主色（靛蓝 600）
  static const Color primary = Color(0xFF4F46E5);

  /// 主色亮阶（靛蓝 400，深色模式）
  static const Color primaryLight = Color(0xFF818CF8);

  /// 按亮度取 Cupertino 主题
  ///
  /// @param brightness 亮度
  /// @return Cupertino 主题
  static CupertinoThemeData data(Brightness brightness) => CupertinoThemeData(
        brightness: brightness,
        primaryColor: brightness == Brightness.dark ? primaryLight : primary,
      );

  /// 次要标签色（随亮度解析）
  static Color secondaryLabel(BuildContext context) =>
      CupertinoDynamicColor.resolve(CupertinoColors.secondaryLabel, context);

  /// 分隔线色（随亮度解析）
  static Color separator(BuildContext context) =>
      CupertinoDynamicColor.resolve(CupertinoColors.separator, context);

  /// 次级分组背景色（随亮度解析）
  static Color secondaryBackground(BuildContext context) =>
      CupertinoDynamicColor.resolve(
        CupertinoColors.secondarySystemGroupedBackground,
        context,
      );

  AppTheme._();
}
