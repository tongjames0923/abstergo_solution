import '../constants/tbs_constants.dart' as c;
import '../exception/tbs_exceptions.dart';
import '../http/ajax_response.dart';
import '../http/base_http_client.dart';
import '../http/request_options.dart';
import '../model/login_grant_model.dart';
import '../model/token_model.dart';
import '../model/user_info_model.dart';
import 'third_party_auth_code.dart';
import 'token_store.dart';

/// 认证与用户能域 API
///
/// 对齐 Java 客户端 LoginApi 与前端 app-ui-vue 登录链路：统一登录端点按
/// 授权类型换取令牌（登录成功后写入令牌存储，供 HTTP 层自动注入请求头），
/// 并提供用户信息、登出、部门切换与登录方式元数据查询。
class AuthApi {
  final BaseHttpClient _httpClient;

  final TokenStore _tokenStore;

  /// 创建认证 API
  ///
  /// @param httpClient 统一 HTTP 客户端
  /// @param tokenStore 令牌存储
  AuthApi({required BaseHttpClient httpClient, required TokenStore tokenStore})
      : _httpClient = httpClient,
        _tokenStore = tokenStore;

  /// 统一登录授权：按授权类型换取访问令牌
  ///
  /// @param grantType  授权类型标识（password/wecom/third_party 等）
  /// @param credential 凭证参数（字段由各授权方式表单契约约定）
  /// @return 令牌模型（登录成功后自动写入令牌存储）
  Future<TokenModel> loginByGrant(
    String grantType,
    Map<String, dynamic> credential,
  ) async {
    final response = await _httpClient.post(
      c.pathLogin + grantType,
      body: credential,
    );
    final tokenModel = _parseToken(response);
    await _tokenStore.write(tokenModel.token ?? '');
    return tokenModel;
  }

  /// 账号密码登录
  ///
  /// @param username 用户名
  /// @param password 密码
  /// @param rememberMe 记住我（透传给服务端，Web 端触发种刷新 Cookie）
  /// @return 令牌模型
  Future<TokenModel> loginByPassword(
    String username,
    String password, {
    bool rememberMe = false,
  }) {
    return loginByGrant(c.grantPassword, <String, dynamic>{
      'username': username,
      'password': password,
      'loginType': 1,
      if (rememberMe) 'rememberMe': true,
    });
  }

  /// 企业微信 SSO 登录
  ///
  /// @param code  企微授权码（OAuth 回调换取）
  /// @param state 防 CSRF 状态串（可选）
  /// @return 令牌模型
  Future<TokenModel> loginByWecom(String code, {String? state}) {
    return loginByGrant(c.grantWecom, <String, dynamic>{
      'code': code,
      if (state != null) 'state': state,
    });
  }

  /// 第三方应用授权登录
  ///
  /// @param appId     应用标识
  /// @param appSecret 应用密钥（仅用于本地生成授权码，不随请求传输）
  /// @return 令牌模型
  Future<TokenModel> loginByThirdParty(String appId, String appSecret) {
    final timestamp = DateTime.now().millisecondsSinceEpoch;
    final code = ThirdPartyAuthCode.generate(appId, appSecret, timestamp: timestamp);
    return loginByGrant(c.grantThirdParty, <String, dynamic>{
      'appId': appId,
      'code': code,
      'timestamp': timestamp,
    });
  }

  /// 获取当前登录用户信息
  ///
  /// @return 用户信息模型
  Future<UserInfoModel> userInfo() async {
    final response = await _httpClient.get(c.pathUserInfo);
    final data = response.data;
    if (data is! Map) {
      throw const TbsClientException('用户信息响应结构异常');
    }
    return UserInfoModel.fromJson(Map<String, dynamic>.from(data));
  }

  /// 查询服务端启用的登录方式元数据
  ///
  /// @return 登录方式列表
  Future<List<LoginGrantModel>> listEnabledGrants() async {
    final response = await _httpClient.get(c.pathLoginGrants);
    return LoginGrantModel.listFromJson(response.dataAsList());
  }

  /// 登出（后端注销令牌 + 本地清理令牌）
  ///
  /// 登出请求为隔离型请求（不走统一登录态处理），后端失败不阻断本地清理。
  Future<void> logout() async {
    try {
      await _httpClient.get(
        c.pathLogout,
        options: const TbsRequestOptions(skipAuthHandling: true),
      );
    } finally {
      await _tokenStore.clear();
    }
  }

  /// 设置当前选中部门（切换后建议重拉用户信息刷新权限上下文）
  ///
  /// @param departmentId 部门 ID（字符串形态，服务端 Long 路径变量）
  Future<void> switchDepartment(String departmentId) async {
    await _httpClient.post('${c.pathSetCurrentDepartment}$departmentId');
  }

  /// 触发服务端种刷新令牌 Cookie（Web 端"记住我"专用，原生端无意义）
  Future<void> plantRefreshCookie() async {
    await _httpClient.get(c.pathGetRefreshToken);
  }

  /// 解析登录响应中的令牌模型并校验令牌非空
  ///
  /// @param response 登录响应信封
  /// @return 令牌模型
  TokenModel _parseToken(AjaxResponse response) {
    final tokenModel =
        TokenModel.fromJson(response.dataAsMap());
    final token = tokenModel.token;
    if (token == null || token.isEmpty) {
      throw const TbsClientException('登录响应缺少令牌');
    }
    return tokenModel;
  }
}
