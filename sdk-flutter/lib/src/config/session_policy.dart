import '../constants/tbs_constants.dart';

/// 错误码到会话行为的映射策略
///
/// 后端规范（docs/guideline/04）与前端 app-ui-vue 实际行为对 405/468 的
/// 处理语义存在矛盾（规范：405 重登、468 刷新；前端代码：405 刷新重放、468 重登），
/// 本 SDK 将"业务码 → 强制重登 / 无感刷新并重放"的映射做成可配置策略：
///
/// - [native] 原生端预设（Android/iOS/桌面）：405/468 一律归为需重登，
///   对齐 Java/C# 客户端（刷新令牌依赖 HttpOnly Cookie，属 Web 专属）。
/// - [web] Web 端预设：对齐 app-ui-vue 实际运行行为（405 触发 Cookie 刷新并
///   重放请求、468 强制重登）。该映射待与后端核对后在两份预设中固化。
class SessionPolicy {
  /// 触发强制重登的业务码集合
  final Set<int> reloginCodes;

  /// 触发无感刷新并重放的业务码集合（需 Web Cookie 流支持）
  final Set<int> refreshCodes;

  /// 创建会话策略
  const SessionPolicy({required this.reloginCodes, required this.refreshCodes});

  /// 原生端预设：401/405/468 一律重登
  static const SessionPolicy native = SessionPolicy(
    reloginCodes: {codeUnauthorized, codeRelogin, codeRefreshFail},
    refreshCodes: {},
  );

  /// Web 端预设：405 刷新重放、468/401 强制重登（对齐 Vue 实际行为，待后端核对）
  static const SessionPolicy web = SessionPolicy(
    reloginCodes: {codeUnauthorized, codeRefreshFail},
    refreshCodes: {codeRelogin},
  );

  /// 判断业务码是否触发强制重登
  ///
  /// @param code 服务端统一响应业务码
  /// @return 命中重登策略时为 true
  bool requiresRelogin(int code) => reloginCodes.contains(code);

  /// 判断业务码是否触发无感刷新
  ///
  /// @param code 服务端统一响应业务码
  /// @return 命中刷新策略且刷新码集合非空时为 true
  bool allowsRefresh(int code) =>
      refreshCodes.isNotEmpty && refreshCodes.contains(code);
}
