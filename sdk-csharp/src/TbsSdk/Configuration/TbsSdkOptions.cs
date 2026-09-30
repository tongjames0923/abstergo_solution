using System;
using System.Collections.Generic;

namespace TbsSdk.Configuration
{
    /// <summary>
    /// SDK 日志级别（诊断用途）。
    /// </summary>
    public enum TbsSdkLogLevel
    {
        Debug,
        Info,
        Warn,
        Error
    }

    /// <summary>
    /// SDK 全局配置项。
    /// <para>错误码默认值与前端 settings.js 及后端 AjaxResult 常量对齐：</para>
    /// <para>200 成功；405 重新登录（token 失效）；468 刷新失败（Web 专属，App 模式同样触发重登）；</para>
    /// <para>601 调试；701 通知；401 未授权；403 无权限；500 服务器错误。</para>
    /// </summary>
    public sealed class TbsSdkOptions
    {
        /// <summary>
        /// 后端接口基地址（必须为绝对地址，如 http://localhost:8083 或经代理的 http://host/api）。
        /// </summary>
        public string BaseUrl { get; set; } = "http://localhost:8083";

        /// <summary>单次 HTTP 请求超时（SSE 长连接不受此超时限制，走流式读取）。</summary>
        public TimeSpan Timeout { get; set; } = TimeSpan.FromSeconds(30);

        /// <summary>语言头 Accept-Language 的取值（对齐前端：zh_CN / en_US）。</summary>
        public string Language { get; set; } = "zh_CN";

        /// <summary>令牌请求头名称（后端 tbs.web.auth.token-filed-name，默认 Authorization，裸 token 无 Bearer 前缀）。</summary>
        public string TokenHeaderName { get; set; } = "Authorization";

        /// <summary>业务成功码。</summary>
        public int SuccessCode { get; set; } = 200;

        /// <summary>未登录错误码集合（触发重登）。</summary>
        public ISet<int> NoLoginCodes { get; } = new HashSet<int> { 405 };

        /// <summary>刷新令牌失败错误码集合（Web 专属链路，App 模式下与未登录同等处理）。</summary>
        public ISet<int> RefreshFailCodes { get; } = new HashSet<int> { 468 };

        /// <summary>调试信息错误码集合。</summary>
        public ISet<int> DebugCodes { get; } = new HashSet<int> { 601 };

        /// <summary>通知信息错误码集合。</summary>
        public ISet<int> NoticeCodes { get; } = new HashSet<int> { 701 };

        /// <summary>是否允许携带 Cookie（默认关闭；App 模式无 Cookie 依赖，开启后可复用 Web 的刷新令牌链路）。</summary>
        public bool UseCookies { get; set; }

        /// <summary>SSE 推送默认参数。</summary>
        public PushOptions Push { get; } = new PushOptions();

        /// <summary>诊断日志回调（可空）。</summary>
        public Action<TbsSdkLogLevel, string> OnLog { get; set; }

        internal void Log(TbsSdkLogLevel level, string message)
        {
            var handler = OnLog;
            if (handler != null)
            {
                handler(level, message);
            }
        }
    }

    /// <summary>
    /// SSE 推送默认参数（对齐前端 PUSH_DEFAULTS 与后端 PushFrameConstants）。
    /// </summary>
    public sealed class PushOptions
    {
        /// <summary>per-user SIGNAL 频道主题前缀（与后端 CHANNEL_TOPIC_PREFIX 一致）。</summary>
        public string TopicPrefix { get; set; } = "tbs.push.channel.";

        /// <summary>心跳上行周期（须远小于后端空闲超时，默认 30s）。</summary>
        public TimeSpan HeartbeatInterval { get; set; } = TimeSpan.FromSeconds(30);

        /// <summary>重连退避基数。</summary>
        public TimeSpan ReconnectBaseDelay { get; set; } = TimeSpan.FromSeconds(1);

        /// <summary>重连退避上限。</summary>
        public TimeSpan ReconnectMaxDelay { get; set; } = TimeSpan.FromSeconds(30);

        /// <summary>重连退避随机抖动上限（防惊群）。</summary>
        public TimeSpan ReconnectJitter { get; set; } = TimeSpan.FromSeconds(1);

        /// <summary>协议版本号（与后端 PROTOCOL_VERSION 一致）。</summary>
        public int ProtocolVersion { get; set; } = 1;
    }
}
