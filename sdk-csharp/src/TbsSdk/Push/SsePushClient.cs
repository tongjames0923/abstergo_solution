using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;
using TbsSdk.Configuration;
using TbsSdk.Core;

namespace TbsSdk.Push
{
    /// <summary>
    /// SSE 推送连接状态。
    /// </summary>
    public enum ConnectionStatus
    {
        /// <summary>空闲（未启动）。</summary>
        Idle,
        /// <summary>建连中。</summary>
        Connecting,
        /// <summary>已连接（hello 握手完成）。</summary>
        Connected,
        /// <summary>重连中（退避等待或换票建连）。</summary>
        Reconnecting,
        /// <summary>已关闭（主动关闭，不再重连）。</summary>
        Closed
    }

    /// <summary>
    /// hello 握手帧载荷。
    /// </summary>
    public sealed class HelloInfo
    {
        /// <summary>会话标识（后续上行帧均需携带）。</summary>
        public string SessionId { get; set; }

        /// <summary>协议版本（可空）。</summary>
        public int? Version { get; set; }

        /// <summary>服务端推荐心跳周期（毫秒，可空；空表示未下发，沿用本地 <c>Push.HeartbeatInterval</c>）。</summary>
        public long? HeartbeatIntervalMillis { get; set; }

        /// <summary>服务端最迟心跳期限（毫秒，可空；距上次心跳超过此值判定链路陈旧应主动重连）。</summary>
        public long? HeartbeatDeadlineMillis { get; set; }
    }

    /// <summary>
    /// SSE 推送连接管理器（对齐前端 push/sse-client.js 的完整协议流程）。
    /// <para>
    /// 协议层统一连接流程：认证签票据 → SSE 建连（ticket 作 query 参数，同时携带与 HTTP 同源的
    /// Authorization 令牌头）→ 收 hello 帧取 sessionId → 上行 SUBSCRIBE 订阅全部主题 →
    /// 周期 PING 保活。一次性 ticket 被服务端原子消费，故断线后必须换票重连，
    /// 退避策略为指数退避 + 随机抖动（1s 起，上限 30s）。
    /// </para>
    /// <para>主题与心跳/退避参数可通过 <see cref="TbsSdkOptions.Push"/> 覆盖；
    /// 提供 <see cref="SubscribeTopics"/>/<see cref="UnsubscribeTopics"/> 支持连接就绪后的动态增删主题。</para>
    /// </summary>
    public sealed class SsePushClient : IDisposable
    {
        private readonly TbsHttpClient _http;
        private readonly TbsSdkOptions _options;
        private readonly object _topicsSync = new object();
        private readonly List<string> _topics = new List<string>();
        private readonly Random _random = new Random();

        private string _token;
        private string _userId;
        private volatile string _sessionId;
        private volatile ConnectionStatus _status = ConnectionStatus.Idle;
        private volatile bool _manualClose;
        private int _reconnectAttempts;
        private CancellationTokenSource _lifetimeCts;
        private Task _connectLoop;
        private Timer _heartbeatTimer;

        /// <summary>服务端 hello 下发的推荐心跳周期（毫秒，null 表示未下发，沿用本地默认）。</summary>
        private volatile long? _recommendedHeartbeatMillis;

        /// <summary>服务端 hello 下发的最迟心跳期限（毫秒，null 表示未下发，不启用陈旧守卫）。</summary>
        private volatile long? _recommendedHeartbeatDeadlineMillis;

        /// <summary>上次收到服务端 PONG 的时间戳（Unix 毫秒），用于最迟期限陈旧判定。</summary>
        private volatile long _lastPongAt;

        /// <summary>本次建连尝试的取消源（心跳陈旧守卫据此打断阻塞的下行读以触发换票重连）。</summary>
        private volatile CancellationTokenSource _attemptCts;

        /// <summary>连接状态变更回调（对齐前端 onStatus）。</summary>
        public event Action<ConnectionStatus> StatusChanged;

        /// <summary>
        /// 收到 SIGNAL 信令或建连成功（hello 兜底）时触发（对齐前端 onSignal）。
        /// 信令本身无载荷，仅提示"该频道有新消息"，宿主应触发消息拉取。
        /// </summary>
        public event Action SignalReceived;

        /// <summary>hello 握手完成（携带 sessionId）。</summary>
        public event Action<HelloInfo> Hello;

        /// <summary>收到任意下行协议帧（signal/pong/data 等已按事件名归一化为帧结构）。</summary>
        public event Action<PushFrame> FrameReceived;

        /// <summary>连接异常（即将换票重连）。</summary>
        public event Action<Exception> ErrorOccurred;

        /// <summary>当前状态。</summary>
        public ConnectionStatus Status => _status;

        /// <summary>当前会话标识（握手完成后可用）。</summary>
        public string SessionId => _sessionId;

        /// <summary>当前订阅主题快照。</summary>
        public IReadOnlyList<string> Topics
        {
            get
            {
                lock (_topicsSync)
                {
                    return _topics.ToArray();
                }
            }
        }

        /// <summary>是否已主动关闭（关闭后不再重连）。</summary>
        public bool IsManuallyClosed => _manualClose;

        public SsePushClient(TbsHttpClient http, TbsSdkOptions options)
        {
            _http = http ?? throw new ArgumentNullException(nameof(http));
            _options = options ?? new TbsSdkOptions();
        }

        /// <summary>
        /// 配置连接参数（须在 <see cref="Connect"/> 前调用；重连后复用同一配置）。
        /// </summary>
        /// <param name="token">当前登录令牌。</param>
        /// <param name="userId">当前登录用户数字 id（缺省据此拼接 SIGNAL 频道 topicPrefix + userId）。</param>
        /// <param name="topics">订阅主题（缺省为 per-user 频道）。</param>
        public void Configure(string token, string userId = null, IEnumerable<string> topics = null)
        {
            if (string.IsNullOrEmpty(token))
            {
                throw new ArgumentException("登录令牌不能为空", nameof(token));
            }
            _token = token;
            _userId = userId;
            lock (_topicsSync)
            {
                _topics.Clear();
                _topics.AddRange(ResolveTopics(topics));
            }
        }

        /// <summary>
        /// 建立推送连接（幂等：连接循环已在运行则忽略）。
        /// </summary>
        public void Connect()
        {
            var existing = _connectLoop;
            if (existing != null && !existing.IsCompleted)
            {
                return;
            }
            if (string.IsNullOrEmpty(_token))
            {
                throw new InvalidOperationException("必须先调用 Configure 提供登录令牌");
            }

            _manualClose = false;
            _reconnectAttempts = 0;
            _lifetimeCts = new CancellationTokenSource();
            var token = _lifetimeCts.Token;
            _connectLoop = Task.Run(() => ConnectLoopAsync(token));
        }

        /// <summary>
        /// 动态追加订阅主题：连接就绪时立即上行订阅帧；未就绪时仅登记，
        /// 待 hello 握手后随全量主题统一订阅。重复主题幂等忽略。
        /// </summary>
        public void SubscribeTopics(params string[] topics)
        {
            var resolved = ResolveTopics(topics);
            if (resolved.Count == 0)
            {
                return;
            }

            List<string> added;
            lock (_topicsSync)
            {
                added = new List<string>();
                foreach (var topic in resolved)
                {
                    if (!_topics.Contains(topic))
                    {
                        _topics.Add(topic);
                        added.Add(topic);
                    }
                }
            }

            if (_status == ConnectionStatus.Connected && !string.IsNullOrEmpty(_sessionId))
            {
                foreach (var topic in added)
                {
                    UploadFrame(new PushFrame
                    {
                        Version = _options.Push.ProtocolVersion,
                        Type = PushConstants.FrameTypeSubscribe,
                        Topic = topic
                    });
                }
            }
        }

        /// <summary>
        /// 动态移除订阅主题（连接就绪时同步上行退订帧）。
        /// </summary>
        public void UnsubscribeTopics(params string[] topics)
        {
            var resolved = ResolveTopics(topics);
            if (resolved.Count == 0)
            {
                return;
            }

            List<string> removed;
            lock (_topicsSync)
            {
                removed = resolved.FindAll(topic => _topics.Remove(topic));
            }

            if (_status == ConnectionStatus.Connected && !string.IsNullOrEmpty(_sessionId))
            {
                foreach (var topic in removed)
                {
                    UploadFrame(new PushFrame
                    {
                        Version = _options.Push.ProtocolVersion,
                        Type = PushConstants.FrameTypeUnsubscribe,
                        Topic = topic
                    });
                }
            }
        }

        /// <summary>
        /// 主动关闭连接（登出/程序退出调用），置位后不再重连。幂等。
        /// </summary>
        public void Close()
        {
            _manualClose = true;
            StopHeartbeat();
            _sessionId = null;
            _reconnectAttempts = 0;

            var cts = _lifetimeCts;
            try
            {
                cts?.Cancel();
            }
            catch (ObjectDisposedException)
            {
                // 并发关闭时的兜底
            }

            SetStatus(ConnectionStatus.Closed);
        }

        /// <inheritdoc />
        public void Dispose()
        {
            Close();
        }

        // ---- 连接主循环 ----

        private async Task ConnectLoopAsync(CancellationToken cancellationToken)
        {
            while (!_manualClose && !cancellationToken.IsCancellationRequested)
            {
                CancellationTokenSource attemptCts = null;
                try
                {
                    // 每次建连一个独立取消源（链接到全局），供心跳陈旧守卫打断阻塞下行读而不动全局取消
                    attemptCts = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
                    _attemptCts = attemptCts;
                    SetStatus(_reconnectAttempts > 0 ? ConnectionStatus.Reconnecting : ConnectionStatus.Connecting);

                    await OpenOnceAsync(attemptCts.Token).ConfigureAwait(false);
                    // OpenOnce 正常返回 = 服务端关闭了流，视为连接异常以触发换票重连
                    if (!_manualClose)
                    {
                        throw new TbsApiException("SSE 连接被服务端关闭");
                    }
                }
                catch (OperationCanceledException) when (cancellationToken.IsCancellationRequested || _manualClose)
                {
                    break;
                }
                catch (Exception ex)
                {
                    _options.Log(TbsSdkLogLevel.Warn, "[TbsSdk][push] SSE 连接异常，准备重连: " + ex.Message);
                    var errorHandler = ErrorOccurred;
                    if (errorHandler != null)
                    {
                        errorHandler(ex);
                    }
                }
                finally
                {
                    StopHeartbeat();
                    _attemptCts = null;
                    attemptCts?.Dispose();
                }

                if (_manualClose || cancellationToken.IsCancellationRequested)
                {
                    break;
                }

                // 指数退避 + 随机抖动（防惊群）；一次性 ticket 已消费，重连必须换票
                var delay = ComputeReconnectDelay();
                _reconnectAttempts++;
                SetStatus(ConnectionStatus.Reconnecting);
                _options.Log(TbsSdkLogLevel.Info,
                    "[TbsSdk][push] " + (int)delay.TotalMilliseconds + "ms 后第 " + _reconnectAttempts + " 次重连");

                try
                {
                    await Task.Delay(delay, cancellationToken).ConfigureAwait(false);
                }
                catch (OperationCanceledException)
                {
                    break;
                }
            }
        }

        /// <summary>
        /// 单次建连：签票据 → SSE 建连 → 逐行消费事件流直至流结束（响应与流资源在内部释放）。
        /// </summary>
        private async Task OpenOnceAsync(CancellationToken cancellationToken)
        {
            // 1. 认证签发一次性建连凭证（TTL 默认 60s）
            var authResult = await _http.PostAsync(PushConstants.AuthPath,
                new PushAuthForm { Token = _token, UserId = _userId },
                null, new TbsRequestOptions { ExplicitToken = _token }, cancellationToken).ConfigureAwait(false);

            if (_manualClose)
            {
                return;
            }

            var credential = authResult.GetData<PushCredential>();
            if (credential == null || string.IsNullOrEmpty(credential.Ticket))
            {
                throw new TbsApiException("推送认证未返回有效 ticket", result: authResult);
            }

            // 2. SSE 建连：票据作 query 参数（对齐前端），令牌走 Authorization 头（原生 HttpClient 无需 polyfill）
            var connectUrlQuery = new Dictionary<string, object>
            {
                ["push-ticket"] = credential.Ticket
            };
            var response = await _http.SendForStreamAsync(System.Net.Http.HttpMethod.Get,
                PushConstants.SseConnectPath, connectUrlQuery,
                new TbsRequestOptions { ExplicitToken = _token }, cancellationToken).ConfigureAwait(false);

            if (!response.IsSuccessStatusCode)
            {
                var status = (int)response.StatusCode;
                response.Dispose();
                throw new TbsApiException("SSE 建连失败: HTTP " + status, httpStatusCode: status);
            }

            // 3. 流式消费事件流
            using (response)
            using (var stream = await response.Content.ReadAsStreamAsync().ConfigureAwait(false))
            using (var reader = new StreamReader(stream, Encoding.UTF8))
            {
                var parser = new SseEventParser();
                while (true)
                {
                    var line = await reader.ReadLineAsync().ConfigureAwait(false);
                    if (line == null)
                    {
                        return; // 流结束（服务端关闭）
                    }
                    cancellationToken.ThrowIfCancellationRequested();

                    var sseEvent = parser.FeedLine(line);
                    if (sseEvent != null)
                    {
                        HandleEvent(sseEvent);
                        if (_manualClose)
                        {
                            return;
                        }
                    }
                }
            }
        }

        // ---- 事件处理 ----

        private void HandleEvent(SseEvent sseEvent)
        {
            switch (sseEvent.Event)
            {
                case PushConstants.EventHello:
                    HandleHello(sseEvent);
                    break;
                case PushConstants.EventSignal:
                    // 信令无载荷，仅提示频道有新消息，由宿主触发拉取
                    SignalReceived?.Invoke();
                    break;
                case PushConstants.EventPong:
                    // 服务端对 PING 的应答，刷新活性时间戳供最迟期限守卫使用
                    _lastPongAt = NowMs();
                    FrameReceived?.Invoke(ParseFrame(sseEvent));
                    break;
                case PushConstants.EventData:
                    FrameReceived?.Invoke(ParseFrame(sseEvent));
                    break;
                case PushConstants.EventError:
                    // 服务端错误帧：一次性 ticket 已消费，必须换票重连
                    throw new TbsApiException("收到 SSE error 帧: " + sseEvent.Data);
                default:
                    FrameReceived?.Invoke(ParseFrame(sseEvent));
                    break;
            }
        }

        private void HandleHello(SseEvent sseEvent)
        {
            var frame = ParseFrame(sseEvent);
            var hello = ParseHelloPayload(frame?.Payload);
            if (hello == null || string.IsNullOrEmpty(hello.SessionId))
            {
                _options.Log(TbsSdkLogLevel.Warn, "[TbsSdk][push] hello 帧解析失败，无法获取 sessionId");
                return;
            }

            _sessionId = hello.SessionId;
            _recommendedHeartbeatMillis = hello.HeartbeatIntervalMillis;
            _recommendedHeartbeatDeadlineMillis = hello.HeartbeatDeadlineMillis;
            _lastPongAt = NowMs();
            SubscribeAllRegistered();
            StartHeartbeat();
            _reconnectAttempts = 0;
            SetStatus(ConnectionStatus.Connected);

            var helloHandler = Hello;
            if (helloHandler != null)
            {
                helloHandler(hello);
            }

            // hello 握手成功后主动触发一次兜底拉取：补齐离线期间消息与丢失的信令
            SignalReceived?.Invoke();
        }

        /// <summary>
        /// 解析并校验 hello 帧 payload（JSON 字符串需二次解析），提取会话标识。
        /// 对齐前端 parseHelloPayload。
        /// </summary>
        public static HelloInfo ParseHelloPayload(string rawPayload)
        {
            if (string.IsNullOrEmpty(rawPayload))
            {
                return null;
            }

            try
            {
                using (var document = JsonDocument.Parse(rawPayload))
                {
                    var root = document.RootElement;
                    if (root.ValueKind != JsonValueKind.Object)
                    {
                        return null;
                    }

                    string sessionId = null;
                    if (root.TryGetProperty("sessionId", out var sessionElement) &&
                        sessionElement.ValueKind == JsonValueKind.String)
                    {
                        sessionId = sessionElement.GetString();
                    }
                    if (string.IsNullOrEmpty(sessionId))
                    {
                        return null;
                    }

                    int? version = null;
                    if (root.TryGetProperty("version", out var versionElement) &&
                        versionElement.ValueKind == JsonValueKind.Number &&
                        versionElement.TryGetInt32(out var versionValue))
                    {
                        version = versionValue;
                    }

                    long? heartbeatInterval = PositiveInt64(root, "heartbeatInterval");
                    long? heartbeatDeadline = PositiveInt64(root, "heartbeatDeadline");

                    return new HelloInfo
                    {
                        SessionId = sessionId,
                        Version = version,
                        HeartbeatIntervalMillis = heartbeatInterval,
                        HeartbeatDeadlineMillis = heartbeatDeadline
                    };
                }
            }
            catch (JsonException)
            {
                return null;
            }
        }

        /// <summary>
        /// 从 JSON 对象提取正数长整型毫秒值：缺失/非数字/非正数返回 null（视为服务端未下发）。
        /// </summary>
        private static long? PositiveInt64(JsonElement root, string name)
        {
            if (root.TryGetProperty(name, out var element) &&
                element.ValueKind == JsonValueKind.Number &&
                element.TryGetInt64(out var value) &&
                value > 0)
            {
                return value;
            }
            return null;
        }

        /// <summary>当前 Unix 毫秒时间戳。</summary>
        private static long NowMs()
        {
            return DateTimeOffset.Now.ToUnixTimeMilliseconds();
        }

        private static PushFrame ParseFrame(SseEvent sseEvent)
        {
            if (sseEvent == null || string.IsNullOrEmpty(sseEvent.Data))
            {
                return null;
            }
            try
            {
                return JsonSerializer.Deserialize<PushFrame>(sseEvent.Data, TbsSdkJson.DefaultOptions);
            }
            catch (JsonException)
            {
                return null;
            }
        }

        // ---- 主题与上行帧 ----

        private void SubscribeAllRegistered()
        {
            string[] topics;
            lock (_topicsSync)
            {
                topics = _topics.ToArray();
            }
            foreach (var topic in topics)
            {
                UploadFrame(new PushFrame
                {
                    Version = _options.Push.ProtocolVersion,
                    Type = PushConstants.FrameTypeSubscribe,
                    Topic = topic
                });
            }
        }

        /// <summary>
        /// 上行帧（fire-and-forget）：失败仅告警，不影响主流程（对齐前端行为）。
        /// </summary>
        private void UploadFrame(PushFrame frame)
        {
            var sessionId = _sessionId;
            if (string.IsNullOrEmpty(sessionId))
            {
                return;
            }

            frame.Timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds();

            Task.Run(async () =>
            {
                try
                {
                    await _http.PostAsync(PushConstants.SseUploadPath,
                        new PushUploadForm { SessionId = sessionId, Frame = frame },
                        null, new TbsRequestOptions { ExplicitToken = _token }).ConfigureAwait(false);
                }
                catch (Exception ex)
                {
                    _options.Log(TbsSdkLogLevel.Warn,
                        "[TbsSdk][push] 帧上行失败 type=" + frame.Type + " topic=" + frame.Topic + ": " + ex.Message);
                }
            });
        }

        // ---- 心跳与重连 ----

        private void StartHeartbeat()
        {
            StopHeartbeat();
            var interval = ResolveHeartbeatInterval();
            if (interval <= TimeSpan.Zero)
            {
                return;
            }
            _heartbeatTimer = new Timer(OnHeartbeatTick, null, interval, interval);
        }

        /// <summary>
        /// 心跳周期：优先用服务端 hello 下发的推荐值，未下发时回退本地配置。
        /// </summary>
        private TimeSpan ResolveHeartbeatInterval()
        {
            var recommended = _recommendedHeartbeatMillis;
            if (recommended.HasValue && recommended.Value > 0)
            {
                return TimeSpan.FromMilliseconds(recommended.Value);
            }
            return _options.Push.HeartbeatInterval;
        }

        private void OnHeartbeatTick(object state)
        {
            try
            {
                // 会话不存在（尚未握手/已重连中）时跳过本轮心跳
                if (string.IsNullOrEmpty(_sessionId))
                {
                    return;
                }
                // 最迟心跳期限守卫：距上次收到 PONG 已超过后端回收点(deadline)，判定链路已陈旧
                // （下行流半开而阻塞读未报错时尤为有效），打断本次连接以触发换票重连；deadline 未下发时不守卫
                var deadline = _recommendedHeartbeatDeadlineMillis;
                var last = _lastPongAt;
                if (deadline.HasValue && deadline.Value > 0 && last > 0 &&
                    NowMs() - last > deadline.Value)
                {
                    _options.Log(TbsSdkLogLevel.Warn, "[TbsSdk][push] 距上次心跳超过最迟期限，判定链路陈旧，换票重连");
                    _attemptCts?.Cancel();
                    return;
                }
                UploadFrame(new PushFrame
                {
                    Version = _options.Push.ProtocolVersion,
                    Type = PushConstants.FrameTypePing
                });
            }
            catch (Exception ex)
            {
                _options.Log(TbsSdkLogLevel.Warn, "[TbsSdk][push] 心跳上行失败: " + ex.Message);
            }
        }

        private void StopHeartbeat()
        {
            var timer = Interlocked.Exchange(ref _heartbeatTimer, null);
            timer?.Dispose();
        }

        private TimeSpan ComputeReconnectDelay()
        {
            var push = _options.Push;
            var delay = Math.Min(
                push.ReconnectMaxDelay.TotalMilliseconds,
                push.ReconnectBaseDelay.TotalMilliseconds * Math.Pow(2, _reconnectAttempts));
            var jitter = push.ReconnectJitter.TotalMilliseconds;
            if (jitter > 0)
            {
                delay += _random.NextDouble() * jitter;
            }
            return TimeSpan.FromMilliseconds(Math.Max(0, delay));
        }

        // ---- 状态与工具 ----

        private void SetStatus(ConnectionStatus status)
        {
            if (_status == status)
            {
                return;
            }
            _status = status;
            var handler = StatusChanged;
            if (handler != null)
            {
                handler(status);
            }
        }

        /// <summary>
        /// 归一化主题入参：过滤非法项；入参为空时回退为 per-user 缺省 SIGNAL 频道（topicPrefix + userId）。
        /// 对齐前端 _resolveTopics。
        /// </summary>
        private List<string> ResolveTopics(IEnumerable<string> topics)
        {
            var valid = new List<string>();
            if (topics != null)
            {
                foreach (var topic in topics)
                {
                    if (!string.IsNullOrWhiteSpace(topic))
                    {
                        valid.Add(topic.Trim());
                    }
                }
            }
            if (valid.Count > 0)
            {
                return valid;
            }
            return _userId != null
                ? new List<string> { _options.Push.TopicPrefix + _userId }
                : new List<string>();
        }
    }
}
