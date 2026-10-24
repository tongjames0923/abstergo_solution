import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../di/session.dart';
import '../features/auth/login_page.dart';
import '../features/auth/oauth_callback_page.dart';
import '../features/home/home_page.dart';
import '../features/messages/messages_page.dart';
import '../features/profile/profile_page.dart';
import '../shell/adaptive_scaffold.dart';

/// 登录态路由守卫路径
const String kLoginRoute = '/login';

/// OAuth 回调路由
const String kOAuthCallbackRoute = '/oauth/callback';

/// 登录后跳转目标查询键
const String kRedirectQueryKey = 'redirect';

/// 应用路由 Provider（登录态变化驱动守卫刷新）
final appRouterProvider = Provider<GoRouter>((ref) {
  final refreshSignal = ValueNotifier<int>(0);
  ref.listen(sessionProvider, (_, _) => refreshSignal.value++);
  ref.onDispose(refreshSignal.dispose);
  return GoRouter(
    initialLocation: '/home',
    refreshListenable: refreshSignal,
    redirect: (context, state) {
      final session = ref.read(sessionProvider);
      final loggedIn = session.hasValue && session.value != null;
      final onAuthPage = state.matchedLocation == kLoginRoute ||
          state.matchedLocation == kOAuthCallbackRoute;
      if (session.isLoading && session.value == null) {
        return null;
      }
      if (!loggedIn && !onAuthPage) {
        final target = state.uri.toString();
        return '$kLoginRoute?${Uri.encodeComponent(kRedirectQueryKey)}=${Uri.encodeComponent(target)}';
      }
      if (loggedIn && onAuthPage) {
        return '/home';
      }
      return null;
    },
    routes: [
      GoRoute(
        path: kLoginRoute,
        builder: (context, state) => const LoginPage(),
      ),
      GoRoute(
        path: kOAuthCallbackRoute,
        builder: (context, state) => const OAuthCallbackPage(),
      ),
      StatefulShellRoute.indexedStack(
        builder: (context, state, navigationShell) =>
            AdaptiveScaffold(navigationShell: navigationShell),
        branches: [
          StatefulShellBranch(routes: [
            GoRoute(
              path: '/home',
              builder: (context, state) => const HomePage(),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: '/messages',
              builder: (context, state) => const MessagesPage(),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: '/profile',
              builder: (context, state) => const ProfilePage(),
            ),
          ]),
        ],
      ),
    ],
  );
});
