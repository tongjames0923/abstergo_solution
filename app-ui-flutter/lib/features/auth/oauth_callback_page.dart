import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:forui/forui.dart';
import 'package:go_router/go_router.dart';

import '../../di/session.dart';
import '../../shared/l10n/app_localizations.dart';

/// OAuth 回调页：授权码换票并跳转工作台
class OAuthCallbackPage extends ConsumerStatefulWidget {
  /// 创建回调页
  const OAuthCallbackPage({super.key});

  @override
  ConsumerState<OAuthCallbackPage> createState() => _OAuthCallbackPageState();
}

class _OAuthCallbackPageState extends ConsumerState<OAuthCallbackPage> {
  String? _error;

  bool _processing = false;

  @override
  void initState() {
    super.initState();
    Future.microtask(_handleCallback);
  }

  /// 处理回调：code 换票 → 进入工作台
  Future<void> _handleCallback() async {
    if (_processing || !mounted) {
      return;
    }
    final l10n = AppLocalizations.of(context);
    final params = GoRouterState.of(context).uri.queryParameters;
    final code = params['code'];
    if (code == null || code.isEmpty) {
      setState(() => _error = l10n.oauthMissingCode);
      return;
    }
    setState(() => _processing = true);
    try {
      await ref.read(sessionProvider.notifier).loginWecom(code);
      if (mounted) {
        context.go('/home');
      }
    } catch (error) {
      if (mounted) {
        setState(() => _error = l10n.oauthFailed);
      }
    } finally {
      if (mounted) {
        setState(() => _processing = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            if (_error == null) ...[
              const CircularProgressIndicator(),
              const SizedBox(height: 16),
              Text(l10n.oauthProcessing),
            ] else ...[
              Icon(
                Icons.error_outline_rounded,
                size: 40,
                color: context.theme.colors.destructive,
              ),
              const SizedBox(height: 12),
              Text(_error!),
              const SizedBox(height: 16),
              FButton(
                variant: .outline,
                onPress: () => context.go('/login'),
                child: Text(l10n.commonRetry),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
