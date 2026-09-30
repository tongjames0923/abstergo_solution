using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using TbsSdk.Core;

namespace TbsSdk.Auth
{
    /// <summary>
    /// 登录授权模块：统一授权端点 POST /public/user/login/{grantType} 的各授权方式封装，
    /// 以及登录方式元数据、用户信息、登出。
    /// <para>以 App 模式登录为主（password + loginType=1）；令牌刷新依赖 Cookie 属 Web 专属，
    /// 本模块不实现，会话失效（405/468）由宿主重新调用登录接口。</para>
    /// </summary>
    public sealed class TbsAuthClient
    {
        /// <summary>授权类型：账号密码。</summary>
        public const string GrantPassword = "password";

        /// <summary>授权类型：企业微信 SSO。</summary>
        public const string GrantWecom = "wecom";

        /// <summary>授权类型：第三方应用。</summary>
        public const string GrantThirdParty = "third_party";

        /// <summary>授权类型：刷新令牌（Web 专属，App 模式不可用——依赖 HttpOnly Cookie）。</summary>
        public const string GrantRefresh = "refresh";

        private readonly TbsHttpClient _http;
        private readonly ITokenStore _tokenStore;

        public TbsAuthClient(TbsHttpClient http, ITokenStore tokenStore)
        {
            _http = http ?? throw new ArgumentNullException(nameof(http));
            _tokenStore = tokenStore;
        }

        /// <summary>
        /// 账号密码登录（App 模式：loginType=1）。
        /// </summary>
        /// <returns>令牌信息（已写入令牌存储）。</returns>
        public Task<TokenModel> LoginByPasswordAsync(string username, string password,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(username))
            {
                throw new ArgumentException("用户名不能为空", nameof(username));
            }
            if (password == null)
            {
                throw new ArgumentException("密码不能为空", nameof(password));
            }

            return LoginByGrantAsync(GrantPassword,
                new LoginForm { Username = username, Password = password, LoginType = 1 },
                cancellationToken);
        }

        /// <summary>
        /// 企业微信 SSO 登录（企微 OAuth 授权码换票）。
        /// </summary>
        public Task<TokenModel> LoginWecomAsync(string code, string state = null,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrEmpty(code))
            {
                throw new ArgumentException("企微授权码不能为空", nameof(code));
            }

            return LoginByGrantAsync(GrantWecom, new WecomLoginForm { Code = code, State = state },
                cancellationToken);
        }

        /// <summary>
        /// 第三方应用登录：按约定算法生成授权码并换票（以第三方应用代理用户身份签发令牌）。
        /// </summary>
        /// <param name="appId">第三方应用标识。</param>
        /// <param name="appSecret">第三方应用密钥。</param>
        /// <param name="timestamp">毫秒时间戳，缺省取当前时间（服务端窗口 ±3 分钟）。</param>
        /// <param name="cancellationToken">取消令牌。</param>
        public Task<TokenModel> LoginThirdPartyAsync(string appId, string appSecret, long? timestamp = null,
            CancellationToken cancellationToken = default)
        {
            var ts = timestamp ?? DateTimeOffset.Now.ToUnixTimeMilliseconds();
            var form = new ThirdPartyLoginForm
            {
                AppId = appId,
                Timestamp = ts,
                Code = ThirdPartyAuthCode.Generate(appId, appSecret, ts)
            };
            return LoginByGrantAsync(GrantThirdParty, form, cancellationToken);
        }

        /// <summary>
        /// 通用授权入口：任意 grantType + 凭证对象，登录成功自动写入令牌存储。
        /// </summary>
        /// <param name="grantType">授权类型标识（与后端 ILoginGrantProvider 对齐）。</param>
        /// <param name="credential">凭证对象（字段由各授权方式表单契约约定）。</param>
        /// <param name="cancellationToken">取消令牌。</param>
        public async Task<TokenModel> LoginByGrantAsync(string grantType, object credential,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrEmpty(grantType))
            {
                throw new ArgumentException("grantType 不能为空", nameof(grantType));
            }

            var result = await _http.PostAsync("/public/user/login/" + grantType, credential,
                null, null, cancellationToken).ConfigureAwait(false);

            var token = result.GetData<TokenModel>();
            if (token == null || !token.IsValid)
            {
                throw new TbsApiException("登录响应缺少有效令牌信息", result: result);
            }

            _tokenStore?.Set(token);
            return token;
        }

        /// <summary>
        /// 获取当前启用的登录方式元数据（GET /public/user/login-grants）。
        /// </summary>
        public Task<List<LoginGrantDescriptor>> GetLoginGrantsAsync(CancellationToken cancellationToken = default)
        {
            return _http.GetAsync<List<LoginGrantDescriptor>>("/public/user/login-grants",
                null, null, cancellationToken);
        }

        /// <summary>
        /// 获取当前登录用户信息（GET /private/user/info）。
        /// </summary>
        public Task<UserInfoModel> GetUserInfoAsync(CancellationToken cancellationToken = default)
        {
            return _http.GetAsync<UserInfoModel>("/private/user/info", null, null, cancellationToken);
        }

        /// <summary>
        /// 登出（GET /private/user/logout）：后端注销令牌与刷新令牌。
        /// 注意：本方法不清理本地令牌存储，请配合 TbsSdkClient.LogoutAsync 或自行调用 ITokenStore.Clear。
        /// </summary>
        public Task LogoutAsync(CancellationToken cancellationToken = default)
        {
            return _http.GetAsync("/private/user/logout", null, null, cancellationToken);
        }
    }

    /// <summary>
    /// 登录方式元数据（GET /public/user/login-grants 的数组项）。
    /// </summary>
    public sealed class LoginGrantDescriptor
    {
        /// <summary>授权类型标识（如 password / wecom / third_party）。</summary>
        public string GrantType { get; set; }

        /// <summary>展示文案 i18n key。</summary>
        public string LabelKey { get; set; }

        /// <summary>公开配置（如企微 corpId/agentId）。</summary>
        public IDictionary<string, System.Text.Json.JsonElement> PublicOptions { get; set; }
    }
}
