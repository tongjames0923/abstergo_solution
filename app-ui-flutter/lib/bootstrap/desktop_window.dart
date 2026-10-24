import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart' show Size;
import 'package:window_manager/window_manager.dart';

/// 桌面窗口初始化（仅 Win/macOS/Linux 生效，Web/移动为空操作）
class DesktopWindow {
  /// 配置窗口尺寸、标题与居中显示
  ///
  /// @param title 窗口标题
  static Future<void> init(String title) async {
    if (kIsWeb || !(Platform.isWindows || Platform.isMacOS || Platform.isLinux)) {
      return;
    }
    await windowManager.ensureInitialized();
    const options = WindowOptions(
      size: Size(1280, 800),
      minimumSize: Size(960, 640),
      center: true,
      titleBarStyle: TitleBarStyle.normal,
    );
    await windowManager.waitUntilReadyToShow(options, () async {
      await windowManager.setTitle(title);
      await windowManager.show();
      await windowManager.focus();
    });
  }

  DesktopWindow._();
}
