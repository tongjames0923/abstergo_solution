import 'package:flutter/material.dart';
import 'package:forui/forui.dart';

/// 设计令牌：靛蓝主色 + 中性底色（shadcn/ui 风格）
///
/// 基于 forui FColors.copyWith 定制主色为靛蓝（#4F46E5），
/// 深浅色双主题统一令牌，桌面/触屏双形态由 [FThemeData.touch] 区分。
class AppTheme {
  /// 主色（靛蓝 600）
  static const Color primary = Color(0xFF4F46E5);

  /// 主色浅阶（靛蓝 500）
  static const Color primaryLight = Color(0xFF6366F1);

  /// 主色深阶（靛蓝 700）
  static const Color primaryDark = Color(0xFF4338CA);

  /// 浅色配色（中性底 + 靛蓝主色）
  static final FColors lightColors = FColors.neutralLight.copyWith(
    primary: primary,
    primaryForeground: const Color(0xFFFAFAFA),
  );

  /// 深色配色（中性底 + 靛蓝亮阶）
  static final FColors darkColors = FColors.neutralDark.copyWith(
    primary: const Color(0xFF818CF8),
    primaryForeground: const Color(0xFF09090B),
  );

  /// 浅色桌面主题
  static final FThemeData lightDesktop = FThemeData(
    touch: false,
    debugLabel: 'IndigoLightDesktop',
    colors: lightColors,
  );

  /// 浅色触屏主题
  static final FThemeData lightTouch = FThemeData(
    touch: true,
    debugLabel: 'IndigoLightTouch',
    colors: lightColors,
  );

  /// 深色桌面主题
  static final FThemeData darkDesktop = FThemeData(
    touch: false,
    debugLabel: 'IndigoDarkDesktop',
    colors: darkColors,
  );

  /// 深色触屏主题
  static final FThemeData darkTouch = FThemeData(
    touch: true,
    debugLabel: 'IndigoDarkTouch',
    colors: darkColors,
  );

  /// 按亮度与形态取 forui 主题数据
  ///
  /// @param brightness 亮度
  /// @param touch 是否触屏形态
  /// @return forui 主题数据
  static FThemeData foruiData(Brightness brightness, {bool touch = false}) {
    final dark = brightness == Brightness.dark;
    return dark
        ? (touch ? darkTouch : darkDesktop)
        : (touch ? lightTouch : lightDesktop);
  }

  /// 按亮度取近似 Material 主题（供 MaterialApp/Material 桥接组件使用）
  ///
  /// @param brightness 亮度
  /// @param touch 是否触屏形态
  /// @return Material 主题
  static ThemeData materialData(Brightness brightness, {bool touch = false}) {
    return foruiData(brightness, touch: touch).toApproximateMaterialTheme();
  }

  AppTheme._();
}
