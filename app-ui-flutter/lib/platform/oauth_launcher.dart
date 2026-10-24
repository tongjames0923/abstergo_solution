import 'package:url_launcher/url_launcher.dart';

/// OAuth 跳转启动器
///
/// 企微 SSO 走浏览器跳转（Web 端同源回调）；移动端 deep-link 授权为二期预留。
class OAuthLauncher {
  /// 企微 SSO 授权地址（对齐 Vue 端 identity/authorize.js）
  ///
  /// @param corpId  企业 ID（来自 login-grants publicOptions）
  /// @param agentId 应用 ID（可选）
  /// @param redirectUri 回调地址（Web 端取当前 origin）
  /// @return 授权 URL
  static Uri wecomAuthorizeUrl({
    required String corpId,
    String? agentId,
    required String redirectUri,
  }) {
    return Uri.https('login.work.weixin.qq.com', '/wwlogin/sso/login', {
      'login_type': 'CorpApp',
      'appid': corpId,
      if (agentId != null && agentId.isNotEmpty) 'agentid': agentId,
      'redirect_uri': redirectUri,
      'state': 'wecom-sso',
    });
  }

  /// 打开外部授权页
  ///
  /// @param url 授权地址
  /// @return 是否成功发起跳转
  static Future<bool> launch(Uri url) async {
    return launchUrl(url, mode: LaunchMode.externalApplication);
  }

  OAuthLauncher._();
}
