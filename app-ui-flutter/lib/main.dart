import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app.dart';
import 'bootstrap/bootstrap.dart';

/// 应用入口：启动编排 → Riverpod 注入 → 根组件
Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await bootstrap('TBS Workspace');
  runApp(const ProviderScope(child: TbsApp()));
}
