using System;
using System.Text.Json.Serialization;

namespace TbsSdk.Auth
{
    /// <summary>
    /// 登录令牌模型（对齐后端 SimpleTokenModel / ITokenModel）。
    /// <para>令牌为不透明随机串（非 JWT），有效期由后端 tbs.web.auth.token-expire-time 控制。</para>
    /// </summary>
    public sealed class TokenModel
    {
        /// <summary>访问令牌（裸值，请求头 Authorization 直接携带，无 Bearer 前缀）。</summary>
        [JsonPropertyName("token")]
        public string Token { get; set; }

        /// <summary>登录用户名。</summary>
        [JsonPropertyName("userName")]
        public string UserName { get; set; }

        /// <summary>账号状态。</summary>
        [JsonPropertyName("status")]
        public string Status { get; set; }

        /// <summary>令牌类型（如 WEB_ACCESS_TOKEN / THIRD_PARTY_ACCESS_TOKEN）。</summary>
        [JsonPropertyName("tokenType")]
        public string TokenType { get; set; }

        /// <summary>会话标识。</summary>
        [JsonPropertyName("sessionId")]
        public string SessionId { get; set; }

        /// <summary>创建方式（如 LOGIN / THIRD）。</summary>
        [JsonPropertyName("createType")]
        public string CreateType { get; set; }

        /// <summary>登录终端 MAC 地址（后端可选采集）。</summary>
        [JsonPropertyName("macAddr")]
        public string MacAddr { get; set; }

        /// <summary>登录 IP（后端可选采集）。</summary>
        [JsonPropertyName("ipAddr")]
        public string IpAddr { get; set; }

        /// <summary>过期时间（毫秒时间戳或字符串日期均可解析）。</summary>
        [JsonPropertyName("expireTime")]
        public DateTime? ExpireTime { get; set; }

        /// <summary>创建时间。</summary>
        [JsonPropertyName("createTime")]
        public DateTime? CreateTime { get; set; }

        /// <summary>令牌是否有效（非空）。</summary>
        public bool IsValid => !string.IsNullOrEmpty(Token);

        /// <summary>令牌是否已按本地时钟过期（后端为准，仅作预判）。</summary>
        public bool IsExpired => ExpireTime.HasValue && ExpireTime.Value <= DateTime.Now;
    }
}
