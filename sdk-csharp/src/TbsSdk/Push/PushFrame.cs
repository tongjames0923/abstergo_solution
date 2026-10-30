using System.Collections.Generic;
using System.Text.Json.Serialization;

namespace TbsSdk.Push
{
    /// <summary>
    /// 推送协议帧（对齐后端 PushFrame）。上行与下行共用同一结构。
    /// </summary>
    public sealed class PushFrame
    {
        /// <summary>协议版本号（缺省 1，与后端 PROTOCOL_VERSION 一致）。</summary>
        [JsonPropertyName("version")]
        public int Version { get; set; } = 1;

        /// <summary>帧类型：上行传大写枚举名（SUBSCRIBE/UNSUBSCRIBE/PING），下行 SSE 事件名为小写值。</summary>
        [JsonPropertyName("type")]
        public string Type { get; set; }

        /// <summary>消息标识（可选）。</summary>
        [JsonPropertyName("messageId")]
        public string MessageId { get; set; }

        /// <summary>主题（SUBSCRIBE/UNSUBSCRIBE 必填，≤128 字符）。</summary>
        [JsonPropertyName("topic")]
        public string Topic { get; set; }

        /// <summary>序列号（可选）。</summary>
        [JsonPropertyName("seq")]
        public long? Seq { get; set; }

        /// <summary>毫秒时间戳。</summary>
        [JsonPropertyName("timestamp")]
        public long Timestamp { get; set; }

        /// <summary>载荷（hello 帧为 JSON 字符串，需二次解析取 sessionId）。</summary>
        [JsonPropertyName("payload")]
        public string Payload { get; set; }

        /// <summary>协议错误码（仅 error 帧携带：1001 版本不兼容 / 1002 帧损坏 / 1003 主题非法 / 1008 订阅未授权 / 1009 上行被拒）。</summary>
        [JsonPropertyName("code")]
        public int? Code { get; set; }
    }

    /// <summary>
    /// 上行帧请求体（对齐后端 SseUploadForm）。
    /// </summary>
    public sealed class PushUploadForm
    {
        /// <summary>hello 握手确认帧下发的会话标识（必填）。</summary>
        [JsonPropertyName("sessionId")]
        public string SessionId { get; set; }

        /// <summary>协议帧。</summary>
        [JsonPropertyName("frame")]
        public PushFrame Frame { get; set; }
    }

    /// <summary>
    /// 推送认证请求体（对齐后端 PushAuthForm）。
    /// </summary>
    public sealed class PushAuthForm
    {
        /// <summary>当前登录令牌（必填）。</summary>
        [JsonPropertyName("token")]
        public string Token { get; set; }

        /// <summary>业务用户标识（可选，供后端审计）。</summary>
        [JsonPropertyName("userId")]
        public string UserId { get; set; }

        /// <summary>附加属性（可选）。</summary>
        [JsonPropertyName("attributes")]
        public IDictionary<string, string> Attributes { get; set; }
    }

    /// <summary>
    /// 推送建连凭证（对齐后端 PushCredential）：一次性、TTL 默认 60s，须尽快用于 SSE 建连。
    /// </summary>
    public sealed class PushCredential
    {
        /// <summary>一次性建连票据（被服务端原子消费，断线后必须重新签发）。</summary>
        [JsonPropertyName("ticket")]
        public string Ticket { get; set; }

        /// <summary>预分配会话标识（以 hello 握手确认帧下发值为准）。</summary>
        [JsonPropertyName("sessionId")]
        public string SessionId { get; set; }

        /// <summary>过期时间。</summary>
        [JsonPropertyName("expireAt")]
        public System.DateTime? ExpireAt { get; set; }

        /// <summary>附加属性。</summary>
        [JsonPropertyName("attributes")]
        public IDictionary<string, string> Attributes { get; set; }

        /// <summary>
        /// 订阅授权集：服务端签票时点计算的可订阅主题模式列表（支持订阅侧通配）。
        /// 不在集内的主题上行订阅会被服务端拒绝（ERROR 帧 code 1008）；
        /// 为 null 或空表示服务端未启用订阅鉴权（旧版本服务端不返回该字段）。
        /// </summary>
        [JsonPropertyName("grantedTopics")]
        public IList<string> GrantedTopics { get; set; }
    }
}
