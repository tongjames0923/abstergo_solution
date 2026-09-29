using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using TbsSdk.Auth;
using TbsSdk.Configuration;

namespace TbsSdk.Core
{
    /// <summary>
    /// HTTP 请求核心封装（对齐前端 utils/request.js）。
    /// <para>能力：token 头与语言头自动注入；统一响应解包与业务码判定；
    /// 405/468 会话失效事件与异常；隔离型请求；泛化 GET/POST/PUT/DELETE/PATCH。</para>
    /// </summary>
    public sealed class TbsHttpClient : IDisposable
    {
        private readonly HttpMessageHandler _handler;
        private readonly bool _ownsHandler;
        private readonly HttpClient _httpClient;
        private readonly HttpClient _streamHttpClient;
        private readonly TbsSdkOptions _options;
        private readonly ITokenStore _tokenStore;

        /// <summary>
        /// 会话失效事件：收到 405/468 业务码时触发（先于异常抛出）。
        /// 宿主应在事件中引导用户重新登录。
        /// </summary>
        public event EventHandler<TbsSessionExpiredEventArgs> SessionExpired;

        /// <summary>
        /// 创建 HTTP 客户端。
        /// </summary>
        /// <param name="options">SDK 配置。</param>
        /// <param name="tokenStore">令牌存储。</param>
        /// <param name="httpMessageHandler">可替换的底层 Handler（测试/代理场景），为空时按配置创建。</param>
        public TbsHttpClient(TbsSdkOptions options, ITokenStore tokenStore, HttpMessageHandler httpMessageHandler = null)
        {
            _options = options ?? new TbsSdkOptions();
            _tokenStore = tokenStore;

            // 共用同一个 handler 派生两个 HttpClient（disposeHandler=false，由 Dispose 统一释放 handler）：
            // _httpClient 承担普通短请求（受 Timeout 约束）；_streamHttpClient 专供 SSE 长连接，
            // 必须关闭总超时——HttpClient.Timeout 对流式响应是从 SendAsync 起算的整段硬超时，
            // 不会被数据活动重置，否则下行长连接会被周期性掐断导致信令等低频事件丢失。
            _handler = httpMessageHandler ?? BuildHandler(_options);
            _ownsHandler = httpMessageHandler == null;
            _httpClient = new HttpClient(_handler, disposeHandler: false);
            _httpClient.Timeout = _options.Timeout;
            _streamHttpClient = new HttpClient(_handler, disposeHandler: false);
            _streamHttpClient.Timeout = System.Threading.Timeout.InfiniteTimeSpan;
        }

        private static HttpMessageHandler BuildHandler(TbsSdkOptions options)
        {
            if (options.UseCookies)
            {
                // 开启 Cookie 支持后可复用后端 Web 侧的 REFRESH_TOKEN 刷新链路（App 模式默认不需要）
                return new HttpClientHandler
                {
                    UseCookies = true,
                    CookieContainer = new System.Net.CookieContainer(),
                    AllowAutoRedirect = true
                };
            }
            return new HttpClientHandler();
        }

        /// <summary>泛化 GET，返回统一响应体。</summary>
        public Task<AjaxResult> GetAsync(string path, IDictionary<string, object> query = null,
            TbsRequestOptions requestOptions = null, CancellationToken cancellationToken = default)
        {
            return SendAsync(HttpMethod.Get, path, null, query, requestOptions, cancellationToken);
        }

        /// <summary>泛化 GET，返回反序列化后的业务体。</summary>
        public async Task<T> GetAsync<T>(string path, IDictionary<string, object> query = null,
            TbsRequestOptions requestOptions = null, CancellationToken cancellationToken = default)
        {
            var result = await GetAsync(path, query, requestOptions, cancellationToken).ConfigureAwait(false);
            return result.GetData<T>();
        }

        /// <summary>泛化 POST，返回统一响应体。</summary>
        public Task<AjaxResult> PostAsync(string path, object body = null, IDictionary<string, object> query = null,
            TbsRequestOptions requestOptions = null, CancellationToken cancellationToken = default)
        {
            return SendAsync(HttpMethod.Post, path, body, query, requestOptions, cancellationToken);
        }

        /// <summary>泛化 POST，返回反序列化后的业务体。</summary>
        public async Task<T> PostAsync<T>(string path, object body = null, IDictionary<string, object> query = null,
            TbsRequestOptions requestOptions = null, CancellationToken cancellationToken = default)
        {
            var result = await PostAsync(path, body, query, requestOptions, cancellationToken).ConfigureAwait(false);
            return result.GetData<T>();
        }

        /// <summary>泛化 PUT，返回统一响应体。</summary>
        public Task<AjaxResult> PutAsync(string path, object body = null, IDictionary<string, object> query = null,
            TbsRequestOptions requestOptions = null, CancellationToken cancellationToken = default)
        {
            return SendAsync(HttpMethod.Put, path, body, query, requestOptions, cancellationToken);
        }

        /// <summary>泛化 DELETE，返回统一响应体。</summary>
        public Task<AjaxResult> DeleteAsync(string path, IDictionary<string, object> query = null,
            TbsRequestOptions requestOptions = null, CancellationToken cancellationToken = default)
        {
            return SendAsync(HttpMethod.Delete, path, null, query, requestOptions, cancellationToken);
        }

        /// <summary>泛化 PATCH（netstandard2.0 无 HttpMethod.Patch 常量，此处显式构造），返回统一响应体。</summary>
        public Task<AjaxResult> PatchAsync(string path, object body = null, IDictionary<string, object> query = null,
            TbsRequestOptions requestOptions = null, CancellationToken cancellationToken = default)
        {
            return SendAsync(new HttpMethod("PATCH"), path, body, query, requestOptions, cancellationToken);
        }

        /// <summary>
        /// 统一请求入口：发送请求 → 读取响应体 → 解包 AjaxResult → 按业务码判定成功/会话失效/普通失败。
        /// </summary>
        public async Task<AjaxResult> SendAsync(HttpMethod method, string path, object body = null,
            IDictionary<string, object> query = null, TbsRequestOptions requestOptions = null,
            CancellationToken cancellationToken = default)
        {
            var response = await SendCoreAsync(_httpClient, method, path, body, query, requestOptions,
                HttpCompletionOption.ResponseContentRead, cancellationToken).ConfigureAwait(false);

            string responseBody;
            using (response)
            {
                responseBody = await response.Content.ReadAsStringAsync().ConfigureAwait(false);
            }

            return ProcessResponse(response, responseBody, requestOptions);
        }

        /// <summary>
        /// 流式请求入口（SSE 建连专用）：仅等待响应头即返回，不读取/解包响应体，
        /// 调用方自行接管响应流并负责释放。
        /// </summary>
        public Task<HttpResponseMessage> SendForStreamAsync(HttpMethod method, string path,
            IDictionary<string, object> query = null, TbsRequestOptions requestOptions = null,
            CancellationToken cancellationToken = default)
        {
            // SSE 建连走无总超时的专用 client（见构造函数注释），避免长连接被 Timeout 周期掐断
            return SendCoreAsync(_streamHttpClient, method, path, null, query, requestOptions,
                HttpCompletionOption.ResponseHeadersRead, cancellationToken);
        }

        private async Task<HttpResponseMessage> SendCoreAsync(HttpClient client, HttpMethod method, string path, object body,
            IDictionary<string, object> query, TbsRequestOptions requestOptions,
            HttpCompletionOption completionOption, CancellationToken cancellationToken)
        {
            var url = BuildUrl(path, query);

            HttpRequestMessage request = null;
            try
            {
                request = new HttpRequestMessage(method, url);
                ApplyHeaders(request, requestOptions);

                if (body != null)
                {
                    var json = System.Text.Json.JsonSerializer.Serialize(body, TbsSdkJson.DefaultOptions);
                    request.Content = new StringContent(json, Encoding.UTF8, "application/json");
                }

                return await client.SendAsync(request, completionOption, cancellationToken)
                    .ConfigureAwait(false);
            }
            catch (HttpRequestException ex)
            {
                throw new TbsApiException("网络请求失败: " + ex.Message, innerException: ex);
            }
            catch (TaskCanceledException ex) when (!cancellationToken.IsCancellationRequested)
            {
                throw new TbsApiException("请求超时（" + (int)_options.Timeout.TotalSeconds + "s）", innerException: ex);
            }
            finally
            {
                request?.Dispose();
            }
        }

        private AjaxResult ProcessResponse(HttpResponseMessage response, string responseBody,
            TbsRequestOptions requestOptions)
        {
            var httpStatus = (int)response.StatusCode;

            System.Text.Json.JsonElement root;
            try
            {
                using (var document = System.Text.Json.JsonDocument.Parse(responseBody))
                {
                    root = document.RootElement.Clone();
                }
            }
            catch (System.Text.Json.JsonException)
            {
                // 非 JSON 响应：直接失败（SSE/文件流等特殊响应请走 SendForStreamAsync / 自行处理）
                throw new TbsApiException(
                    response.IsSuccessStatusCode
                        ? "响应不是有效的 JSON，无法解包 AjaxResult"
                        : "HTTP " + httpStatus + " " + response.StatusCode + "（响应非 JSON）",
                    httpStatusCode: httpStatus,
                    responseBody: Truncate(responseBody));
            }

            var result = AjaxResult.Parse(root);

            // 隔离型请求：不做统一登录态处理，直接返回原始响应体
            if (requestOptions != null && requestOptions.SkipAuthHandling)
            {
                return result;
            }

            if (!result.Code.HasValue)
            {
                throw new TbsApiException("响应缺少 code 字段，无法判定业务结果",
                    httpStatusCode: httpStatus, responseBody: Truncate(responseBody), result: result);
            }

            if (result.Code.Value == _options.SuccessCode)
            {
                return result;
            }

            // 405（token 失效）/ 468（刷新失败）：会话失效，通知宿主重新登录
            if (_options.NoLoginCodes.Contains(result.Code.Value) || _options.RefreshFailCodes.Contains(result.Code.Value))
            {
                _options.Log(TbsSdkLogLevel.Warn,
                    "[TbsSdk] 会话失效 code=" + result.Code.Value + " message=" + result.Message);
                var handler = SessionExpired;
                if (handler != null)
                {
                    handler(this, new TbsSessionExpiredEventArgs(result.Code.Value, result.Message));
                }
                throw new TbsReloginRequiredException(
                    string.IsNullOrWhiteSpace(result.Message) ? "会话已失效，请重新登录" : result.Message,
                    result.Code.Value, httpStatus, result);
            }

            throw new TbsApiException(
                string.IsNullOrWhiteSpace(result.Message) ? "请求失败（code=" + result.Code.Value + "）" : result.Message,
                result.Code.Value, result.Redirect, httpStatus, Truncate(responseBody), result);
        }

        private void ApplyHeaders(HttpRequestMessage request, TbsRequestOptions requestOptions)
        {
            var explicitToken = requestOptions?.ExplicitToken;
            if (!string.IsNullOrEmpty(explicitToken))
            {
                request.Headers.TryAddWithoutValidation(_options.TokenHeaderName, explicitToken);
            }
            else
            {
                var token = _tokenStore?.Get()?.Token;
                if (!string.IsNullOrEmpty(token))
                {
                    request.Headers.TryAddWithoutValidation(_options.TokenHeaderName, token);
                }
            }

            request.Headers.TryAddWithoutValidation("Accept-Language",
                string.IsNullOrEmpty(_options.Language) ? "zh_CN" : _options.Language);
        }

        private string BuildUrl(string path, IDictionary<string, object> query)
        {
            var builder = new StringBuilder((_options.BaseUrl ?? string.Empty).TrimEnd('/'));

            if (!string.IsNullOrEmpty(path))
            {
                if (!path.StartsWith("/", StringComparison.Ordinal))
                {
                    builder.Append('/');
                }
                builder.Append(path);
            }

            if (query != null && query.Count > 0)
            {
                var first = true;
                foreach (var pair in query)
                {
                    if (pair.Value == null)
                    {
                        continue;
                    }
                    builder.Append(first ? '?' : '&');
                    first = false;
                    builder.Append(Uri.EscapeDataString(pair.Key));
                    builder.Append('=');
                    builder.Append(Uri.EscapeDataString(ToQueryStringValue(pair.Value)));
                }
            }

            return builder.ToString();
        }

        private static string ToQueryStringValue(object value)
        {
            switch (value)
            {
                case bool boolValue:
                    return boolValue ? "true" : "false";
                case DateTime dateTime:
                    return dateTime.ToString("yyyy-MM-dd HH:mm:ss", System.Globalization.CultureInfo.InvariantCulture);
                case IFormattable formattable:
                    return formattable.ToString(null, System.Globalization.CultureInfo.InvariantCulture);
                default:
                    return value.ToString();
            }
        }

        private static string Truncate(string text, int maxLength = 512)
        {
            if (string.IsNullOrEmpty(text) || text.Length <= maxLength)
            {
                return text;
            }
            return text.Substring(0, maxLength) + "...(truncated)";
        }

        public void Dispose()
        {
            _httpClient.Dispose();
            _streamHttpClient.Dispose();
            // 仅释放由本类创建的 handler；外部注入的 handler 归调用方所有
            if (_ownsHandler)
            {
                _handler.Dispose();
            }
        }
    }
}
