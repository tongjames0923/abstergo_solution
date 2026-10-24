// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appName => 'TBS Workspace';

  @override
  String get navHome => 'Home';

  @override
  String get navMessages => 'Messages';

  @override
  String get navProfile => 'Me';

  @override
  String get loginTitle => 'Sign in';

  @override
  String get loginWelcome => 'Welcome back';

  @override
  String get loginSubtitle => 'Sign in to continue to your workspace';

  @override
  String get loginUsername => 'Username';

  @override
  String get loginUsernameHint => 'Enter your username';

  @override
  String get loginPassword => 'Password';

  @override
  String get loginPasswordHint => 'Enter your password';

  @override
  String get loginRememberMe => 'Remember me';

  @override
  String get loginSubmit => 'Sign in';

  @override
  String get loginSubmitting => 'Signing in…';

  @override
  String get loginFailed => 'Sign-in failed';

  @override
  String get loginWecomButton => 'Continue with WeCom';

  @override
  String get loginThirdPartyTitle => 'Third-party app auth';

  @override
  String get loginAppId => 'App ID';

  @override
  String get loginAppSecret => 'App Secret';

  @override
  String get loginUnsupportedGrant =>
      'This login method is not supported on this platform yet';

  @override
  String get loginGatewayVersion => 'Gateway version';

  @override
  String get loginGrantPassword => 'Password';

  @override
  String get loginGrantWecom => 'WeCom';

  @override
  String get loginGrantThirdParty => 'Third-party app';

  @override
  String get homePushChannel => 'Realtime push';

  @override
  String get loginGrantsLoadFailed =>
      'Failed to load login methods, password sign-in is shown by default';

  @override
  String get oauthProcessing => 'Processing authorization callback…';

  @override
  String get oauthMissingCode =>
      'Authorization callback is missing the code parameter';

  @override
  String get oauthFailed => 'Authorization sign-in failed';

  @override
  String get homeMorning => 'Good morning';

  @override
  String get homeAfternoon => 'Good afternoon';

  @override
  String get homeEvening => 'Good evening';

  @override
  String get homeUnreadCount => 'Unread messages';

  @override
  String get homeSessionOnline => 'Online';

  @override
  String get homeSessionOffline => 'Disconnected';

  @override
  String get homeSessionReconnecting => 'Reconnecting';

  @override
  String get homeSessionConnecting => 'Connecting';

  @override
  String get homeQuickActions => 'Quick actions';

  @override
  String get homeLatestMessages => 'Latest messages';

  @override
  String get homeViewAll => 'View all';

  @override
  String get homeNoMessages => 'No messages';

  @override
  String get homeCheckUpdate => 'Check updates';

  @override
  String get homeNotLoggedIn => 'Not signed in';

  @override
  String get messagesTitle => 'Messages';

  @override
  String get messagesFilterAll => 'All';

  @override
  String get messagesFilterUnread => 'Unread';

  @override
  String get messagesMarkRead => 'Mark as read';

  @override
  String get messagesMarkAllRead => 'Mark all read';

  @override
  String get messagesEmpty => 'No messages';

  @override
  String get messagesLoading => 'Loading…';

  @override
  String get messagesNoMore => 'No more messages';

  @override
  String get messagesDisconnected => 'Realtime push disconnected';

  @override
  String get messagesReconnect => 'Reconnect';

  @override
  String get messagesSender => 'Sender';

  @override
  String get messagesPriority => 'Priority';

  @override
  String get profileTitle => 'Me';

  @override
  String get profileDepartment => 'Current department';

  @override
  String get profileDepartmentHint => 'Choose a department to switch';

  @override
  String get profileTheme => 'Appearance';

  @override
  String get profileThemeSystem => 'System';

  @override
  String get profileThemeLight => 'Light';

  @override
  String get profileThemeDark => 'Dark';

  @override
  String get profileLanguage => 'Language';

  @override
  String get profileLogout => 'Sign out';

  @override
  String get profileLogoutConfirm => 'Sign out of this account?';

  @override
  String get profileVersion => 'App version';

  @override
  String get profileCheckUpdate => 'Check updates';

  @override
  String get profileUpToDate => 'You are up to date';

  @override
  String profileNewVersion(String version) {
    return 'New version $version available';
  }

  @override
  String get profileUpdateNow => 'Update now';

  @override
  String get profileUpdateLater => 'Later';

  @override
  String profileUpdateDownloading(int progress) {
    return 'Downloading $progress%';
  }

  @override
  String get profileUpdateDone => 'Download complete';

  @override
  String get profileUpdateFailed => 'Download failed';

  @override
  String get profileUpdateForce => 'Force update';

  @override
  String get profileUpdateRecommend => 'Recommended';

  @override
  String get profileUpdateNotify => 'Optional';

  @override
  String get profileAccountStatus => 'Account status';

  @override
  String get commonCancel => 'Cancel';

  @override
  String get commonConfirm => 'OK';

  @override
  String get commonRetry => 'Retry';

  @override
  String get commonLoading => 'Loading…';

  @override
  String get commonError => 'Something went wrong';

  @override
  String get commonRefresh => 'Refresh';
}
