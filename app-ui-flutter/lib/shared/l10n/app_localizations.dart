import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_en.dart';
import 'app_localizations_zh.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('en'),
    Locale('zh'),
  ];

  /// No description provided for @appName.
  ///
  /// In zh, this message translates to:
  /// **'小童工作台'**
  String get appName;

  /// No description provided for @navHome.
  ///
  /// In zh, this message translates to:
  /// **'首页'**
  String get navHome;

  /// No description provided for @navMessages.
  ///
  /// In zh, this message translates to:
  /// **'消息'**
  String get navMessages;

  /// No description provided for @navProfile.
  ///
  /// In zh, this message translates to:
  /// **'我的'**
  String get navProfile;

  /// No description provided for @loginTitle.
  ///
  /// In zh, this message translates to:
  /// **'登录'**
  String get loginTitle;

  /// No description provided for @loginWelcome.
  ///
  /// In zh, this message translates to:
  /// **'欢迎回来'**
  String get loginWelcome;

  /// No description provided for @loginSubtitle.
  ///
  /// In zh, this message translates to:
  /// **'登录以继续使用工作台'**
  String get loginSubtitle;

  /// No description provided for @loginUsername.
  ///
  /// In zh, this message translates to:
  /// **'用户名'**
  String get loginUsername;

  /// No description provided for @loginUsernameHint.
  ///
  /// In zh, this message translates to:
  /// **'请输入用户名'**
  String get loginUsernameHint;

  /// No description provided for @loginPassword.
  ///
  /// In zh, this message translates to:
  /// **'密码'**
  String get loginPassword;

  /// No description provided for @loginPasswordHint.
  ///
  /// In zh, this message translates to:
  /// **'请输入密码'**
  String get loginPasswordHint;

  /// No description provided for @loginRememberMe.
  ///
  /// In zh, this message translates to:
  /// **'记住我'**
  String get loginRememberMe;

  /// No description provided for @loginSubmit.
  ///
  /// In zh, this message translates to:
  /// **'登录'**
  String get loginSubmit;

  /// No description provided for @loginSubmitting.
  ///
  /// In zh, this message translates to:
  /// **'登录中…'**
  String get loginSubmitting;

  /// No description provided for @loginFailed.
  ///
  /// In zh, this message translates to:
  /// **'登录失败'**
  String get loginFailed;

  /// No description provided for @loginWecomButton.
  ///
  /// In zh, this message translates to:
  /// **'跳转企业微信授权'**
  String get loginWecomButton;

  /// No description provided for @loginThirdPartyTitle.
  ///
  /// In zh, this message translates to:
  /// **'第三方应用授权'**
  String get loginThirdPartyTitle;

  /// No description provided for @loginAppId.
  ///
  /// In zh, this message translates to:
  /// **'应用标识'**
  String get loginAppId;

  /// No description provided for @loginAppSecret.
  ///
  /// In zh, this message translates to:
  /// **'应用密钥'**
  String get loginAppSecret;

  /// No description provided for @loginUnsupportedGrant.
  ///
  /// In zh, this message translates to:
  /// **'当前端暂不支持该登录方式'**
  String get loginUnsupportedGrant;

  /// No description provided for @loginGatewayVersion.
  ///
  /// In zh, this message translates to:
  /// **'网关版本'**
  String get loginGatewayVersion;

  /// No description provided for @loginGrantPassword.
  ///
  /// In zh, this message translates to:
  /// **'账号密码'**
  String get loginGrantPassword;

  /// No description provided for @loginGrantWecom.
  ///
  /// In zh, this message translates to:
  /// **'企业微信'**
  String get loginGrantWecom;

  /// No description provided for @loginGrantThirdParty.
  ///
  /// In zh, this message translates to:
  /// **'第三方应用'**
  String get loginGrantThirdParty;

  /// No description provided for @homePushChannel.
  ///
  /// In zh, this message translates to:
  /// **'实时推送'**
  String get homePushChannel;

  /// No description provided for @loginGrantsLoadFailed.
  ///
  /// In zh, this message translates to:
  /// **'登录方式加载失败，已展示默认账号密码登录'**
  String get loginGrantsLoadFailed;

  /// No description provided for @oauthProcessing.
  ///
  /// In zh, this message translates to:
  /// **'正在处理授权回调…'**
  String get oauthProcessing;

  /// No description provided for @oauthMissingCode.
  ///
  /// In zh, this message translates to:
  /// **'授权回调缺少 code 参数'**
  String get oauthMissingCode;

  /// No description provided for @oauthFailed.
  ///
  /// In zh, this message translates to:
  /// **'授权登录失败'**
  String get oauthFailed;

  /// No description provided for @homeMorning.
  ///
  /// In zh, this message translates to:
  /// **'早上好'**
  String get homeMorning;

  /// No description provided for @homeAfternoon.
  ///
  /// In zh, this message translates to:
  /// **'下午好'**
  String get homeAfternoon;

  /// No description provided for @homeEvening.
  ///
  /// In zh, this message translates to:
  /// **'晚上好'**
  String get homeEvening;

  /// No description provided for @homeUnreadCount.
  ///
  /// In zh, this message translates to:
  /// **'未读消息'**
  String get homeUnreadCount;

  /// No description provided for @homeSessionOnline.
  ///
  /// In zh, this message translates to:
  /// **'在线'**
  String get homeSessionOnline;

  /// No description provided for @homeSessionOffline.
  ///
  /// In zh, this message translates to:
  /// **'已断开'**
  String get homeSessionOffline;

  /// No description provided for @homeSessionReconnecting.
  ///
  /// In zh, this message translates to:
  /// **'重连中'**
  String get homeSessionReconnecting;

  /// No description provided for @homeSessionConnecting.
  ///
  /// In zh, this message translates to:
  /// **'连接中'**
  String get homeSessionConnecting;

  /// No description provided for @homeQuickActions.
  ///
  /// In zh, this message translates to:
  /// **'快捷入口'**
  String get homeQuickActions;

  /// No description provided for @homeLatestMessages.
  ///
  /// In zh, this message translates to:
  /// **'最新消息'**
  String get homeLatestMessages;

  /// No description provided for @homeViewAll.
  ///
  /// In zh, this message translates to:
  /// **'查看全部'**
  String get homeViewAll;

  /// No description provided for @homeNoMessages.
  ///
  /// In zh, this message translates to:
  /// **'暂无消息'**
  String get homeNoMessages;

  /// No description provided for @homeCheckUpdate.
  ///
  /// In zh, this message translates to:
  /// **'检查更新'**
  String get homeCheckUpdate;

  /// No description provided for @homeNotLoggedIn.
  ///
  /// In zh, this message translates to:
  /// **'未登录'**
  String get homeNotLoggedIn;

  /// No description provided for @messagesTitle.
  ///
  /// In zh, this message translates to:
  /// **'消息中心'**
  String get messagesTitle;

  /// No description provided for @messagesFilterAll.
  ///
  /// In zh, this message translates to:
  /// **'全部'**
  String get messagesFilterAll;

  /// No description provided for @messagesFilterUnread.
  ///
  /// In zh, this message translates to:
  /// **'未读'**
  String get messagesFilterUnread;

  /// No description provided for @messagesMarkRead.
  ///
  /// In zh, this message translates to:
  /// **'标记已读'**
  String get messagesMarkRead;

  /// No description provided for @messagesMarkAllRead.
  ///
  /// In zh, this message translates to:
  /// **'全部已读'**
  String get messagesMarkAllRead;

  /// No description provided for @messagesEmpty.
  ///
  /// In zh, this message translates to:
  /// **'暂无消息'**
  String get messagesEmpty;

  /// No description provided for @messagesLoading.
  ///
  /// In zh, this message translates to:
  /// **'加载中…'**
  String get messagesLoading;

  /// No description provided for @messagesNoMore.
  ///
  /// In zh, this message translates to:
  /// **'没有更多了'**
  String get messagesNoMore;

  /// No description provided for @messagesDisconnected.
  ///
  /// In zh, this message translates to:
  /// **'实时推送已断开'**
  String get messagesDisconnected;

  /// No description provided for @messagesReconnect.
  ///
  /// In zh, this message translates to:
  /// **'重连'**
  String get messagesReconnect;

  /// No description provided for @messagesSender.
  ///
  /// In zh, this message translates to:
  /// **'发送者'**
  String get messagesSender;

  /// No description provided for @messagesPriority.
  ///
  /// In zh, this message translates to:
  /// **'优先级'**
  String get messagesPriority;

  /// No description provided for @profileTitle.
  ///
  /// In zh, this message translates to:
  /// **'个人中心'**
  String get profileTitle;

  /// No description provided for @profileDepartment.
  ///
  /// In zh, this message translates to:
  /// **'当前部门'**
  String get profileDepartment;

  /// No description provided for @profileDepartmentHint.
  ///
  /// In zh, this message translates to:
  /// **'选择要切换的部门'**
  String get profileDepartmentHint;

  /// No description provided for @profileTheme.
  ///
  /// In zh, this message translates to:
  /// **'外观'**
  String get profileTheme;

  /// No description provided for @profileThemeSystem.
  ///
  /// In zh, this message translates to:
  /// **'跟随系统'**
  String get profileThemeSystem;

  /// No description provided for @profileThemeLight.
  ///
  /// In zh, this message translates to:
  /// **'浅色'**
  String get profileThemeLight;

  /// No description provided for @profileThemeDark.
  ///
  /// In zh, this message translates to:
  /// **'深色'**
  String get profileThemeDark;

  /// No description provided for @profileLanguage.
  ///
  /// In zh, this message translates to:
  /// **'语言'**
  String get profileLanguage;

  /// No description provided for @profileLogout.
  ///
  /// In zh, this message translates to:
  /// **'退出登录'**
  String get profileLogout;

  /// No description provided for @profileLogoutConfirm.
  ///
  /// In zh, this message translates to:
  /// **'确定要退出登录吗？'**
  String get profileLogoutConfirm;

  /// No description provided for @profileVersion.
  ///
  /// In zh, this message translates to:
  /// **'客户端版本'**
  String get profileVersion;

  /// No description provided for @profileCheckUpdate.
  ///
  /// In zh, this message translates to:
  /// **'检查更新'**
  String get profileCheckUpdate;

  /// No description provided for @profileUpToDate.
  ///
  /// In zh, this message translates to:
  /// **'已是最新版本'**
  String get profileUpToDate;

  /// No description provided for @profileNewVersion.
  ///
  /// In zh, this message translates to:
  /// **'发现新版本 {version}'**
  String profileNewVersion(String version);

  /// No description provided for @profileUpdateNow.
  ///
  /// In zh, this message translates to:
  /// **'立即更新'**
  String get profileUpdateNow;

  /// No description provided for @profileUpdateLater.
  ///
  /// In zh, this message translates to:
  /// **'稍后提醒'**
  String get profileUpdateLater;

  /// No description provided for @profileUpdateDownloading.
  ///
  /// In zh, this message translates to:
  /// **'下载中 {progress}%'**
  String profileUpdateDownloading(int progress);

  /// No description provided for @profileUpdateDone.
  ///
  /// In zh, this message translates to:
  /// **'下载完成'**
  String get profileUpdateDone;

  /// No description provided for @profileUpdateFailed.
  ///
  /// In zh, this message translates to:
  /// **'下载失败'**
  String get profileUpdateFailed;

  /// No description provided for @profileUpdateForce.
  ///
  /// In zh, this message translates to:
  /// **'强制更新'**
  String get profileUpdateForce;

  /// No description provided for @profileUpdateRecommend.
  ///
  /// In zh, this message translates to:
  /// **'推荐更新'**
  String get profileUpdateRecommend;

  /// No description provided for @profileUpdateNotify.
  ///
  /// In zh, this message translates to:
  /// **'可选更新'**
  String get profileUpdateNotify;

  /// No description provided for @profileAccountStatus.
  ///
  /// In zh, this message translates to:
  /// **'账号状态'**
  String get profileAccountStatus;

  /// No description provided for @commonCancel.
  ///
  /// In zh, this message translates to:
  /// **'取消'**
  String get commonCancel;

  /// No description provided for @commonConfirm.
  ///
  /// In zh, this message translates to:
  /// **'确定'**
  String get commonConfirm;

  /// No description provided for @commonRetry.
  ///
  /// In zh, this message translates to:
  /// **'重试'**
  String get commonRetry;

  /// No description provided for @commonLoading.
  ///
  /// In zh, this message translates to:
  /// **'加载中…'**
  String get commonLoading;

  /// No description provided for @commonError.
  ///
  /// In zh, this message translates to:
  /// **'出错了'**
  String get commonError;

  /// No description provided for @commonRefresh.
  ///
  /// In zh, this message translates to:
  /// **'刷新'**
  String get commonRefresh;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['en', 'zh'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'en':
      return AppLocalizationsEn();
    case 'zh':
      return AppLocalizationsZh();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}
