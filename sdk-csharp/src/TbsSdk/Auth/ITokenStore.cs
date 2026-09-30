namespace TbsSdk.Auth
{
    /// <summary>
    /// 令牌存储抽象：SDK 登录成功后自动写入，私有请求时自动读取注入请求头。
    /// <para>宿主可实现此接口接入自有持久化（如桌面端加密文件、移动端安全存储），
    /// 以实现跨进程/跨会话的登录态复用。</para>
    /// </summary>
    public interface ITokenStore
    {
        /// <summary>获取当前令牌（未登录时返回 null）。</summary>
        TokenModel Get();

        /// <summary>写入令牌（登录成功时由 SDK 调用）。</summary>
        void Set(TokenModel token);

        /// <summary>清除令牌（登出/会话失效时由 SDK 或宿主调用）。</summary>
        void Clear();
    }

    /// <summary>
    /// 默认内存令牌存储（线程安全）。进程退出即失效，适合服务端/桌面端短生命周期场景。
    /// </summary>
    public sealed class InMemoryTokenStore : ITokenStore
    {
        private readonly object _sync = new object();
        private TokenModel _token;

        public TokenModel Get()
        {
            lock (_sync)
            {
                return _token;
            }
        }

        public void Set(TokenModel token)
        {
            lock (_sync)
            {
                _token = token;
            }
        }

        public void Clear()
        {
            lock (_sync)
            {
                _token = null;
            }
        }
    }
}
