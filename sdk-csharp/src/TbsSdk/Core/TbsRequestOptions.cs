namespace TbsSdk.Core
{
    /// <summary>
    /// 单次请求级选项。
    /// </summary>
    public sealed class TbsRequestOptions
    {
        /// <summary>
        /// 隔离型请求（对齐前端 _skipAuthHandling）：跳过统一登录态处理（不触发 405/468 重登事件），
        /// 直接返回原始响应体，由调用方自行判定 code 并展示结果（如第三方连通性测试）。
        /// </summary>
        public bool SkipAuthHandling { get; set; }

        /// <summary>
        /// 显式令牌（对齐前端“请求头已携带令牌时不覆盖”）：优先于令牌存储中的当前令牌。
        /// </summary>
        public string ExplicitToken { get; set; }
    }
}
