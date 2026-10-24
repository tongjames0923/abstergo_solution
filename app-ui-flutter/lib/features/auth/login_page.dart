import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:forui/forui.dart';
import 'package:go_router/go_router.dart';
import 'package:tbs_client/tbs_client.dart';

import '../../di/providers.dart';
import '../../di/session.dart';
import '../../platform/oauth_launcher.dart';
import '../../routing/app_router.dart';
import '../../shared/l10n/app_localizations.dart';

/// 可用登录方式 Provider
final loginGrantsProvider = FutureProvider<List<LoginGrantModel>>((ref) async {
  final client = ref.watch(tbsClientProvider);
  return client.auth.listEnabledGrants();
});

/// 网关版本 Provider
final gatewayVersionProvider = FutureProvider<String>((ref) async {
  final client = ref.watch(tbsClientProvider);
  return client.gatewayVersionString();
});

/// 登录页：授权方式动态渲染 + 凭证表单 + 记住我 + OAuth 跳转
class LoginPage extends ConsumerStatefulWidget {
  /// 创建登录页
  const LoginPage({super.key});

  @override
  ConsumerState<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends ConsumerState<LoginPage> {
  final TextEditingController _username = TextEditingController();

  final TextEditingController _password = TextEditingController();

  final TextEditingController _appId = TextEditingController();

  final TextEditingController _appSecret = TextEditingController();

  bool _rememberMe = false;

  bool _submitting = false;

  String? _error;

  String _activeGrant = 'password';

  @override
  void dispose() {
    _username.dispose();
    _password.dispose();
    _appId.dispose();
    _appSecret.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final grants = ref.watch(loginGrantsProvider);
    return Scaffold(
      body: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 420),
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                _BrandHeader(l10n: l10n),
                const SizedBox(height: 32),
                grants.when(
                  data: (list) => _GrantTabs(
                    grants: list,
                    activeGrant: _activeGrant,
                    onSelect: (grant) => setState(() => _activeGrant = grant),
                  ),
                  loading: () => const Center(child: CircularProgressIndicator()),
                  error: (_, _) => _GrantTabs(
                    grants: const [],
                    activeGrant: _activeGrant,
                    onSelect: (grant) => setState(() => _activeGrant = grant),
                  ),
                ),
                const SizedBox(height: 20),
                if (_error != null)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: Text(
                      _error!,
                      style: TextStyle(
                        color: context.theme.colors.destructive,
                        fontSize: 13,
                      ),
                    ),
                  ),
                AnimatedSwitcher(
                  duration: const Duration(milliseconds: 200),
                  child: _buildGrantForm(context, l10n),
                ),
                const SizedBox(height: 24),
                _GatewayFooter(l10n: l10n),
              ],
            ),
          ),
        ),
      ),
    );
  }

  /// 按授权类型渲染凭证表单
  Widget _buildGrantForm(BuildContext context, AppLocalizations l10n) {
    switch (_activeGrant) {
      case 'wecom':
        return _WecomForm(l10n: l10n, submitting: _submitting);
      case 'third_party':
        return _ThirdPartyForm(
          l10n: l10n,
          appId: _appId,
          appSecret: _appSecret,
          submitting: _submitting,
        );
      default:
        return _PasswordForm(
          l10n: l10n,
          username: _username,
          password: _password,
          rememberMe: _rememberMe,
          submitting: _submitting,
          onRememberMe: (value) => setState(() => _rememberMe = value),
          onSubmit: _submitPassword,
        );
    }
  }

  /// 提交账号密码登录
  ///
  /// @param l10n 本地化资源
  Future<void> _submitPassword(AppLocalizations l10n) async {
    if (_submitting) {
      return;
    }
    setState(() {
      _submitting = true;
      _error = null;
    });
    try {
      await ref.read(sessionProvider.notifier).loginPassword(
            _username.text.trim(),
            _password.text,
            rememberMe: _rememberMe,
          );
      if (mounted) {
        _navigateAfterLogin();
      }
    } on TbsException catch (error) {
      setState(() => _error = error.message.isEmpty ? l10n.loginFailed : error.message);
    } finally {
      if (mounted) {
        setState(() => _submitting = false);
      }
    }
  }

  /// 登录成功后跳转（恢复被拦截目标页）
  void _navigateAfterLogin() {
    final redirect = GoRouterState.of(context)
        .uri
        .queryParameters[kRedirectQueryKey];
    context.go((redirect == null || redirect.isEmpty) ? '/home' : redirect);
  }
}

/// 品牌区
class _BrandHeader extends StatelessWidget {
  final AppLocalizations l10n;

  const _BrandHeader({required this.l10n});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: context.theme.colors.primary,
                borderRadius: BorderRadius.circular(14),
              ),
              alignment: Alignment.center,
              child: Icon(
                Icons.bolt_rounded,
                size: 26,
                color: context.theme.colors.primaryForeground,
              ),
            ),
            const SizedBox(width: 12),
            Text(l10n.appName, style: context.theme.typography.body.xl2),
          ],
        ),
        const SizedBox(height: 16),
        Text(
          l10n.loginWelcome,
          style: context.theme.typography.body.lg.copyWith(fontWeight: FontWeight.w600),
        ),
        const SizedBox(height: 4),
        Text(
          l10n.loginSubtitle,
          style: TextStyle(color: context.theme.colors.mutedForeground, fontSize: 13),
        ),
      ],
    );
  }
}

/// 授权方式切换（登录方式元数据驱动）
class _GrantTabs extends StatelessWidget {
  final List<LoginGrantModel> grants;

  final String activeGrant;

  final ValueChanged<String> onSelect;

  const _GrantTabs({
    required this.grants,
    required this.activeGrant,
    required this.onSelect,
  });

  @override
  Widget build(BuildContext context) {
    final known = <String>{for (final grant in grants) grant.grantType};
    if (!known.contains('password')) {
      known.add('password');
    }
    final tabs = known.toList(growable: false);
    final l10n = AppLocalizations.of(context);
    String labelOf(String grant) => switch (grant) {
          'password' => l10n.loginGrantPassword,
          'wecom' => l10n.loginGrantWecom,
          'third_party' => l10n.loginGrantThirdParty,
          _ => grant,
        };
    return Wrap(
      spacing: 8,
      children: [
        for (final tab in tabs)
          FButton(
            variant: tab == activeGrant ? .primary : .outline,
            size: .sm,
            onPress: () => onSelect(tab),
            child: Text(labelOf(tab)),
          ),
      ],
    );
  }
}

/// 账号密码表单
class _PasswordForm extends StatelessWidget {
  final AppLocalizations l10n;

  final TextEditingController username;

  final TextEditingController password;

  final bool rememberMe;

  final bool submitting;

  final ValueChanged<bool> onRememberMe;

  final Future<void> Function(AppLocalizations) onSubmit;

  const _PasswordForm({
    required this.l10n,
    required this.username,
    required this.password,
    required this.rememberMe,
    required this.submitting,
    required this.onRememberMe,
    required this.onSubmit,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        FTextField(
          control: .managed(controller: username),
          label: Text(l10n.loginUsername),
          hint: l10n.loginUsernameHint,
        ),
        const SizedBox(height: 14),
        FTextField(
          control: .managed(controller: password),
          label: Text(l10n.loginPassword),
          hint: l10n.loginPasswordHint,
          obscureText: true,
        ),
        const SizedBox(height: 10),
        Row(
          children: [
            FSwitch(value: rememberMe, onChange: onRememberMe),
            const SizedBox(width: 8),
            Text(
              l10n.loginRememberMe,
              style: TextStyle(
                fontSize: 13,
                color: context.theme.colors.mutedForeground,
              ),
            ),
          ],
        ),
        const SizedBox(height: 18),
        FButton(
          onPress: submitting ? null : () => onSubmit(l10n),
          child: Text(submitting ? l10n.loginSubmitting : l10n.loginSubmit),
        ),
      ],
    );
  }
}

/// 企微 SSO 表单（跳转授权）
class _WecomForm extends ConsumerWidget {
  final AppLocalizations l10n;

  final bool submitting;

  const _WecomForm({required this.l10n, required this.submitting});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text(
          l10n.loginUnsupportedGrant,
          style: TextStyle(
            fontSize: 13,
            color: context.theme.colors.mutedForeground,
          ),
        ),
        const SizedBox(height: 14),
        FButton(
          variant: .outline,
          onPress: submitting ? null : () => _launchWecom(context, ref),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.open_in_new_rounded, size: 18),
              const SizedBox(width: 8),
              Text(l10n.loginWecomButton),
            ],
          ),
        ),
      ],
    );
  }

  /// 发起企微 SSO 浏览器跳转
  Future<void> _launchWecom(BuildContext context, WidgetRef ref) async {
    final grants =
        ref.read(loginGrantsProvider).value ?? const <LoginGrantModel>[];
    final wecom = grants.where((g) => g.grantType == 'wecom').firstOrNull;
    final corpId = '${wecom?.publicOptions['corpId'] ?? ''}';
    final agentId = '${wecom?.publicOptions['agentId'] ?? ''}';
    final url = OAuthLauncher.wecomAuthorizeUrl(
      corpId: corpId,
      agentId: agentId,
      redirectUri: Uri.base.origin,
    );
    await OAuthLauncher.launch(url);
  }
}

/// 第三方应用授权表单
class _ThirdPartyForm extends ConsumerWidget {
  final AppLocalizations l10n;

  final TextEditingController appId;

  final TextEditingController appSecret;

  final bool submitting;

  const _ThirdPartyForm({
    required this.l10n,
    required this.appId,
    required this.appSecret,
    required this.submitting,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        FTextField(
          control: .managed(controller: appId),
          label: Text(l10n.loginAppId),
        ),
        const SizedBox(height: 14),
        FTextField(
          control: .managed(controller: appSecret),
          label: Text(l10n.loginAppSecret),
          obscureText: true,
        ),
        const SizedBox(height: 18),
        FButton(
          onPress: submitting ? null : () => _submit(context, ref),
          child: Text(l10n.loginSubmit),
        ),
      ],
    );
  }

  /// 提交第三方授权登录
  Future<void> _submit(BuildContext context, WidgetRef ref) async {
    try {
      await ref
          .read(sessionProvider.notifier)
          .loginThirdParty(appId.text.trim(), appSecret.text);
      if (context.mounted) {
        context.go('/home');
      }
    } on TbsException catch (error) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              error.message.isEmpty ? l10n.loginFailed : error.message,
            ),
          ),
        );
      }
    }
  }
}

/// 网关版本脚注
class _GatewayFooter extends ConsumerWidget {
  final AppLocalizations l10n;

  const _GatewayFooter({required this.l10n});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final version = ref.watch(gatewayVersionProvider);
    return Center(
      child: Text(
        version.when(
          data: (v) => '${l10n.loginGatewayVersion} · ${v.isEmpty ? '-' : v}',
          loading: () => l10n.loginGatewayVersion,
          error: (_, _) => '${l10n.loginGatewayVersion} · -',
        ),
        style: TextStyle(
          fontSize: 11,
          color: context.theme.colors.mutedForeground,
        ),
      ),
    );
  }
}
