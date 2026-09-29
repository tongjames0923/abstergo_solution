using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using TbsSdk.Auth;
using TbsSdk.Configuration;
using TbsSdk.Core;
using TbsSdk.Push;
using TbsSdk.Update;

namespace TbsSdk
{
    /// <summary>
    /// SDK 组合根：聚合认证、HTTP、消息中心与 SSE 推送，提供统一入口。
    /// <para>
    /// 快速接入示例：
    /// <code>
    /// var options = new TbsSdkOptions { BaseUrl = "http://localhost:8083" };
    /// using var sdk = new TbsSdkClient(options);
    /// sdk.SessionExpired += (s, e) => Console.WriteLine("会话失效，请重新登录: " + e.Message);
    /// var token = await sdk.LoginAsync("admin", "123456");
    /// var user = await sdk.Auth.GetUserInfoAsync();
    /// sdk.StartPush(token.Id?.ToString());   // 或显式指定订阅主题
    /// sdk.Push.SignalReceived += () => { /* 触发 sdk.Messages.FetchMessagesAsync 拉取 */ };
    /// </code>
    /// </para>
    /// </summary>
    public sealed class TbsSdkClient : IDisposable
    {
        /// <summary>SDK 配置。</summary>
        public TbsSdkOptions Options { get; }

        /// <summary>令牌存储（登录成功自动写入；宿主可注入自有实现实现持久化）。</summary>
        public ITokenStore TokenStore { get; }

        /// <summary>HTTP 核心（泛化请求入口，覆盖全部业务模块）。</summary>
        public TbsHttpClient Http { get; }

        /// <summary>登录授权模块。</summary>
        public TbsAuthClient Auth { get; }

        /// <summary>推送消息中心模块。</summary>
        public PushMessageClient Messages { get; }

        /// <summary>SSE 推送连接管理器。</summary>
        public SsePushClient Push { get; }

        /// <summary>应用更新模块：升级检查与安装包下载。</summary>
        public AppUpdateClient Update { get; }

        /// <summary>
        /// 会话失效事件（405/468）：App 模式下宿主应引导用户重新登录。
        /// </summary>
        public event EventHandler<TbsSessionExpiredEventArgs> SessionExpired
        {
            add { Http.SessionExpired += value; }
            remove { Http.SessionExpired -= value; }
        }

        /// <summary>
        /// 创建 SDK 客户端。
        /// </summary>
        /// <param name="options">配置（为空时使用默认配置，需自行设置 BaseUrl）。</param>
        /// <param name="httpMessageHandler">可替换底层 Handler（测试/代理场景）。</param>
        /// <param name="tokenStore">令牌存储（为空时使用内存实现）。</param>
        /// <param name="downloadHttpClient">外部注入的安装包下载 HttpClient（可选）。</param>
        public TbsSdkClient(TbsSdkOptions options = null, HttpMessageHandler httpMessageHandler = null,
            ITokenStore tokenStore = null, HttpClient downloadHttpClient = null)
        {
            Options = options ?? new TbsSdkOptions();
            TokenStore = tokenStore ?? new InMemoryTokenStore();
            Http = new TbsHttpClient(Options, TokenStore, httpMessageHandler);
            Auth = new TbsAuthClient(Http, TokenStore);
            Messages = new PushMessageClient(Http);
            Push = new SsePushClient(Http, Options);
            Update = new AppUpdateClient(Http, downloadHttpClient);
        }

        /// <summary>
        /// 账号密码登录（App 模式：loginType=1），成功后令牌写入存储。
        /// </summary>
        public Task<TokenModel> LoginAsync(string username, string password,
            CancellationToken cancellationToken = default)
        {
            return Auth.LoginByPasswordAsync(username, password, cancellationToken);
        }

        /// <summary>
        /// 登录后启动推送连接（对齐前端：用户态就绪后自动建连并订阅 per-user 主题）。
        /// </summary>
        /// <param name="userId">当前登录用户数字 id（用于缺省拼接 per-user SIGNAL 频道）。</param>
        /// <param name="topics">显式订阅主题（缺省为 topicPrefix + userId）。</param>
        public void StartPush(string userId = null, IEnumerable<string> topics = null)
        {
            var token = TokenStore.Get()?.Token;
            if (string.IsNullOrEmpty(token))
            {
                throw new InvalidOperationException("尚未登录，无可用令牌；请先调用 LoginAsync/LoginByGrantAsync");
            }
            Push.Configure(token, userId, topics);
            Push.Connect();
        }

        /// <summary>
        /// 登出：调用后端登出接口，关闭推送长连接并清理本地令牌。
        /// 后端接口失败不阻断本地清理（对齐前端 logout 兜底语义）。
        /// </summary>
        public async Task LogoutAsync(CancellationToken cancellationToken = default)
        {
            try
            {
                await Auth.LogoutAsync(cancellationToken).ConfigureAwait(false);
            }
            finally
            {
                Push.Close();
                TokenStore.Clear();
            }
        }

        /// <summary>
        /// 应用升级检查（公开端点，无需登录）：返回是否需要升级、动作强度与安装包下载地址。
        /// </summary>
        /// <param name="appKey">应用标识。</param>
        /// <param name="platform">平台（如 ANDROID/IOS/WEB，见 AppPlatform）。</param>
        /// <param name="currentVersionCode">客户端当前内部版本号。</param>
        public Task<AppUpdateCheckResult> CheckUpdateAsync(string appKey, string platform,
            long currentVersionCode, CancellationToken cancellationToken = default)
        {
            return Update.CheckUpgradeAsync(appKey, platform, currentVersionCode, cancellationToken);
        }

        /// <summary>仅清理本地登录态（不调用后端）。</summary>
        public void ClearLocalSession()
        {
            Push.Close();
            TokenStore.Clear();
        }

        public void Dispose()
        {
            Push.Dispose();
            Http.Dispose();
        }
    }
}
