/// 应用升级检查与网关版本模型集合
library;

import 'json_utils.dart';

/// 客户端平台常量
///
/// 对齐后端升级检查入参 platform 约定，客户端上报当前运行平台以匹配对应安装包。
class AppPlatform {
  /// 安卓
  static const String android = 'ANDROID';

  /// iOS
  static const String ios = 'IOS';

  /// Web
  static const String web = 'WEB';

  /// Windows
  static const String windows = 'WINDOWS';

  /// macOS
  static const String macos = 'MACOS';

  /// Linux
  static const String linux = 'LINUX';

  AppPlatform._();
}

/// 升级动作编码常量
///
/// 对齐后端 AppVersionCompatibilityActionEnum 的 code 值。
class AppUpdateAction {
  /// 强制更新：低于最低兼容版本时阻断使用直至升级
  static const int force = 1;

  /// 推荐更新：提示用户升级但不阻断使用
  static const int recommend = 2;

  /// 仅提示：告知存在新版本，不做进一步干预
  static const int notify = 3;

  /// 动作编码转可读名称
  ///
  /// @param action 动作编码（可空）
  /// @return force/recommend/notify，未知值或 null 时为 null
  static String? toName(int? action) {
    switch (action) {
      case force:
        return 'force';
      case recommend:
        return 'recommend';
      case notify:
        return 'notify';
      default:
        return null;
    }
  }

  AppUpdateAction._();
}

/// 应用升级检查表单
///
/// 对应服务端 AppVersionCheckForm：公开接口入参，客户端上报应用标识、
/// 平台与当前内部版本号。
class AppUpdateCheckForm {
  /// 应用标识
  final String appKey;

  /// 平台标识（如 ANDROID、IOS、WEB）
  final String platform;

  /// 客户端当前内部版本号
  final int currentVersionCode;

  /// 创建检查表单
  const AppUpdateCheckForm({
    required this.appKey,
    required this.platform,
    required this.currentVersionCode,
  });

  /// 序列化为请求体
  ///
  /// @return 检查表单 JSON 对象
  Map<String, dynamic> toJson() => <String, dynamic>{
        'appKey': appKey,
        'platform': platform,
        'currentVersionCode': currentVersionCode,
      };
}

/// 应用升级检查结果
///
/// 对应服务端 AppVersionUpdateCheckVO：needUpdate 为 false 时其余字段为空，
/// downloadUrls 为最新版本安装包下载地址（OSS 预签名 URL 或外部托管链接）。
class AppUpdateCheckResult {
  /// 是否需要升级
  final bool needUpdate;

  /// 升级动作类型（不需要升级时为 null）
  final int? action;

  /// 最新已发布版本的内部版本号
  final int? latestVersionCode;

  /// 最新已发布版本的外部版本名称
  final String? latestVersionName;

  /// 最新版本的更新日志
  final String? releaseNote;

  /// 安装包下载地址列表（多镜像容灾）
  final List<String> downloadUrls;

  /// 创建检查结果
  const AppUpdateCheckResult({
    required this.needUpdate,
    this.action,
    this.latestVersionCode,
    this.latestVersionName,
    this.releaseNote,
    this.downloadUrls = const [],
  });

  /// 升级动作可读名称（force/recommend/notify），不需要升级时为 null
  ///
  /// @return 动作可读名称
  String? get actionName => AppUpdateAction.toName(action);

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端检查结果对象
  /// @return 检查结果
  static AppUpdateCheckResult fromJson(Map<String, dynamic> json) =>
      AppUpdateCheckResult(
        needUpdate: json['needUpdate'] == true,
        action: (json['action'] as num?)?.toInt(),
        latestVersionCode: parseFlexibleInt(json['latestVersionCode']),
        latestVersionName: json['latestVersionName'] as String?,
        releaseNote: json['releaseNote'] as String?,
        downloadUrls: json['downloadUrls'] is List
            ? (json['downloadUrls'] as List).whereType<String>().toList()
            : <String>[],
      );
}

/// 网关版本信息
///
/// 对应 GET /public/gateway/version 响应 data，客户端可据此判断兼容性。
class GatewayVersionModel {
  /// 网关版本号（Maven 版本注入）
  final String? version;

  /// 视图配置版本号
  final String? viewVersion;

  /// 构建产物标识
  final String? artifact;

  /// 构建时间
  final String? buildTime;

  /// 实例端口
  final String? instancePort;

  /// 创建网关版本信息
  const GatewayVersionModel({
    this.version,
    this.viewVersion,
    this.artifact,
    this.buildTime,
    this.instancePort,
  });

  /// 从 JSON 反序列化
  ///
  /// @param json 服务端版本对象
  /// @return 网关版本信息
  static GatewayVersionModel fromJson(Map<String, dynamic> json) =>
      GatewayVersionModel(
        version: json['version']?.toString(),
        viewVersion: json['viewVersion']?.toString(),
        artifact: json['artifact']?.toString(),
        buildTime: json['buildTime']?.toString(),
        instancePort: json['instancePort']?.toString(),
      );
}
