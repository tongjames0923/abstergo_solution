// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Chinese (`zh`).
class AppLocalizationsZh extends AppLocalizations {
  AppLocalizationsZh([String locale = 'zh']) : super(locale);

  @override
  String get appName => '小童工作台';

  @override
  String get navHome => '首页';

  @override
  String get navMessages => '消息';

  @override
  String get navProfile => '我的';

  @override
  String get loginTitle => '登录';

  @override
  String get loginWelcome => '欢迎回来';

  @override
  String get loginSubtitle => '登录以继续使用工作台';

  @override
  String get loginUsername => '用户名';

  @override
  String get loginUsernameHint => '请输入用户名';

  @override
  String get loginPassword => '密码';

  @override
  String get loginPasswordHint => '请输入密码';

  @override
  String get loginRememberMe => '记住我';

  @override
  String get loginSubmit => '登录';

  @override
  String get loginSubmitting => '登录中…';

  @override
  String get loginFailed => '登录失败';

  @override
  String get loginWecomButton => '跳转企业微信授权';

  @override
  String get loginThirdPartyTitle => '第三方应用授权';

  @override
  String get loginAppId => '应用标识';

  @override
  String get loginAppSecret => '应用密钥';

  @override
  String get loginUnsupportedGrant => '当前端暂不支持该登录方式';

  @override
  String get loginGatewayVersion => '网关版本';

  @override
  String get loginGrantPassword => '账号密码';

  @override
  String get loginGrantWecom => '企业微信';

  @override
  String get loginGrantThirdParty => '第三方应用';

  @override
  String get homePushChannel => '实时推送';

  @override
  String get loginGrantsLoadFailed => '登录方式加载失败，已展示默认账号密码登录';

  @override
  String get oauthProcessing => '正在处理授权回调…';

  @override
  String get oauthMissingCode => '授权回调缺少 code 参数';

  @override
  String get oauthFailed => '授权登录失败';

  @override
  String get homeMorning => '早上好';

  @override
  String get homeAfternoon => '下午好';

  @override
  String get homeEvening => '晚上好';

  @override
  String get homeUnreadCount => '未读消息';

  @override
  String get homeSessionOnline => '在线';

  @override
  String get homeSessionOffline => '已断开';

  @override
  String get homeSessionReconnecting => '重连中';

  @override
  String get homeSessionConnecting => '连接中';

  @override
  String get homeQuickActions => '快捷入口';

  @override
  String get homeLatestMessages => '最新消息';

  @override
  String get homeViewAll => '查看全部';

  @override
  String get homeNoMessages => '暂无消息';

  @override
  String get homeCheckUpdate => '检查更新';

  @override
  String get homeNotLoggedIn => '未登录';

  @override
  String get messagesTitle => '消息中心';

  @override
  String get messagesFilterAll => '全部';

  @override
  String get messagesFilterUnread => '未读';

  @override
  String get messagesMarkRead => '标记已读';

  @override
  String get messagesMarkAllRead => '全部已读';

  @override
  String get messagesEmpty => '暂无消息';

  @override
  String get messagesLoading => '加载中…';

  @override
  String get messagesNoMore => '没有更多了';

  @override
  String get messagesDisconnected => '实时推送已断开';

  @override
  String get messagesReconnect => '重连';

  @override
  String get messagesSender => '发送者';

  @override
  String get messagesPriority => '优先级';

  @override
  String get profileTitle => '个人中心';

  @override
  String get profileDepartment => '当前部门';

  @override
  String get profileDepartmentHint => '选择要切换的部门';

  @override
  String get profileTheme => '外观';

  @override
  String get profileThemeSystem => '跟随系统';

  @override
  String get profileThemeLight => '浅色';

  @override
  String get profileThemeDark => '深色';

  @override
  String get profileLanguage => '语言';

  @override
  String get profileLogout => '退出登录';

  @override
  String get profileLogoutConfirm => '确定要退出登录吗？';

  @override
  String get profileVersion => '客户端版本';

  @override
  String get profileCheckUpdate => '检查更新';

  @override
  String get profileUpToDate => '已是最新版本';

  @override
  String profileNewVersion(String version) {
    return '发现新版本 $version';
  }

  @override
  String get profileUpdateNow => '立即更新';

  @override
  String get profileUpdateLater => '稍后提醒';

  @override
  String profileUpdateDownloading(int progress) {
    return '下载中 $progress%';
  }

  @override
  String get profileUpdateDone => '下载完成';

  @override
  String get profileUpdateFailed => '下载失败';

  @override
  String get profileUpdateForce => '强制更新';

  @override
  String get profileUpdateRecommend => '推荐更新';

  @override
  String get profileUpdateNotify => '可选更新';

  @override
  String get profileAccountStatus => '账号状态';

  @override
  String get commonCancel => '取消';

  @override
  String get commonConfirm => '确定';

  @override
  String get commonRetry => '重试';

  @override
  String get commonLoading => '加载中…';

  @override
  String get commonError => '出错了';

  @override
  String get commonRefresh => '刷新';
}
