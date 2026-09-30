using System.Text.Json.Serialization;

namespace TbsSdk.Auth
{
    /// <summary>
    /// 账号密码登录表单（对齐后端 LoginForm）。
    /// <para>SDK 默认 <see cref="LoginType"/>=1（客户端/App 登录）；0 或缺省为 Web 登录。</para>
    /// </summary>
    public sealed class LoginForm
    {
        [JsonPropertyName("username")]
        public string Username { get; set; }

        [JsonPropertyName("password")]
        public string Password { get; set; }

        /// <summary>登录模式：0/缺省 = Web；1 = 客户端/App。</summary>
        [JsonPropertyName("loginType")]
        public int? LoginType { get; set; } = 1;
    }

    /// <summary>
    /// 企业微信 SSO 登录表单（对齐后端 WecomLoginForm）：企微 OAuth 授权码换票。
    /// </summary>
    public sealed class WecomLoginForm
    {
        /// <summary>企微 OAuth 授权码（必填）。</summary>
        [JsonPropertyName("code")]
        public string Code { get; set; }

        /// <summary>OAuth state（可选）。</summary>
        [JsonPropertyName("state")]
        public string State { get; set; }
    }

    /// <summary>
    /// 第三方应用登录表单（对齐后端 DefaultThirdPartyLoginForm）。
    /// <para>code 由 <see cref="ThirdPartyAuthCode.Generate"/> 按约定算法生成，时间戳窗口 ±3 分钟。</para>
    /// </summary>
    public sealed class ThirdPartyLoginForm
    {
        /// <summary>授权码（必填）。</summary>
        [JsonPropertyName("code")]
        public string Code { get; set; }

        /// <summary>第三方应用标识（必填）。</summary>
        [JsonPropertyName("appId")]
        public string AppId { get; set; }

        /// <summary>毫秒时间戳（必填，±3 分钟窗口）。</summary>
        [JsonPropertyName("timestamp")]
        public long? Timestamp { get; set; }
    }
}
