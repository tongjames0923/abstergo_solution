using System;

namespace TbsSdk.Core
{
    /// <summary>
    /// 后端接口调用异常：承载业务码、消息、redirect 附加字段与 HTTP 状态。
    /// <para>code=200 之外的一切业务失败均以此表达；405/468 以
    /// <see cref="TbsReloginRequiredException"/> 子类表达。</para>
    /// </summary>
    public class TbsApiException : Exception
    {
        public TbsApiException(string message, int? code = null, string redirect = null,
            int? httpStatusCode = null, string responseBody = null, AjaxResult result = null,
            Exception innerException = null)
            : base(message, innerException)
        {
            Code = code;
            Redirect = redirect;
            HttpStatusCode = httpStatusCode;
            ResponseBody = responseBody;
            Result = result;
        }

        /// <summary>业务错误码（如 500/401/403），非业务错误时为 null。</summary>
        public int? Code { get; }

        /// <summary>响应体中的 redirect 附加字段。</summary>
        public string Redirect { get; }

        /// <summary>HTTP 状态码（可获取到响应时）。</summary>
        public int? HttpStatusCode { get; }

        /// <summary>原始响应体（截断后，便于排查）。</summary>
        public string ResponseBody { get; }

        /// <summary>解析出的完整响应（可空）。</summary>
        public AjaxResult Result { get; }
    }

    /// <summary>
    /// 会话失效异常：业务码为 405（token 失效需重登）或 468（刷新失败）时抛出。
    /// <para>App 模式下不实现 Cookie 刷新链路，宿主收到此异常应重新调用登录接口换取令牌。</para>
    /// </summary>
    public sealed class TbsReloginRequiredException : TbsApiException
    {
        public TbsReloginRequiredException(string message, int code, int? httpStatusCode = null,
            AjaxResult result = null)
            : base(message, code, null, httpStatusCode, null, result)
        {
        }
    }

    /// <summary>
    /// 会话失效事件参数。
    /// </summary>
    public sealed class TbsSessionExpiredEventArgs : EventArgs
    {
        public TbsSessionExpiredEventArgs(int code, string message)
        {
            Code = code;
            Message = message;
        }

        /// <summary>触发失效的业务码（405 或 468）。</summary>
        public int Code { get; }

        /// <summary>失效描述。</summary>
        public string Message { get; }
    }
}
