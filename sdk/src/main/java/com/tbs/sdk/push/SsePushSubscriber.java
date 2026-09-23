package com.tbs.sdk.push;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.sdk.auth.TokenManager;
import com.tbs.sdk.config.SdkConfig;
import com.tbs.sdk.constants.SdkConstants;
import com.tbs.sdk.exception.SdkAuthException;
import com.tbs.sdk.exception.SdkClientException;
import com.tbs.sdk.http.IBaseHttpClient;
import com.tbs.sdk.model.FrameType;
import com.tbs.sdk.model.FrameUploadRequest;
import com.tbs.sdk.model.PushAuthForm;
import com.tbs.sdk.model.PushCredential;
import com.tbs.sdk.model.PushFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSE推送订阅器实现
 * <p>
 * 编排组合通道全流程：HTTP客户端走auth端点签发一次性push-ticket（TTL 60秒，
 * 每次建连重新签发，禁止复用）→ OkHttp EventSource携ticket建立SSE下行流 →
 * 收到hello握手帧后记录sessionId并自动SUBSCRIBE初始主题 → 后台定时上行PING心跳 →
 * 断线按指数退避重连（重新走auth）。帧回调在单线程执行器中分发，
 * 回调异常隔离不影响流。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SsePushSubscriber implements ISsePushSubscriber {

    /**
     * 心跳上行间隔（秒）
     */
    private static final int PING_INTERVAL_SECONDS = 30;

    /**
     * 日志
     */
    private static final Logger log = LoggerFactory.getLogger(SsePushSubscriber.class);

    /**
     * JSON处理器（线程安全）
     */
    private final ObjectMapper mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * SSE专用OkHttp客户端（读取超时必须为0，避免空闲长连接被读超时切断）
     */
    private final OkHttpClient sseClient;

    /**
     * 统一HTTP客户端（承担auth与upload端点调用）
     */
    private final IBaseHttpClient httpClient;

    /**
     * SDK配置
     */
    private final SdkConfig config;

    /**
     * 令牌管理器
     */
    private final TokenManager tokenManager;

    /**
     * 回调分发与重连调度执行器（单线程保证回调有序）
     */
    private final ScheduledExecutorService scheduler;

    /**
     * 会话标识（hello握手成功后写入）
     */
    private final AtomicReference<String> sessionIdRef = new AtomicReference<>();

    /**
     * 关闭标志
     */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * 连续重连次数（握手成功后清零）
     */
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    /**
     * 当前SSE事件流（volatile保证close与其他线程可见）
     */
    private volatile EventSource eventSource;

    /**
     * 心跳定时任务句柄
     */
    private volatile ScheduledFuture<?> pingFuture;

    /**
     * 订阅参数（subscribe时固化）
     */
    private volatile List<String> topics = new ArrayList<>();

    /**
     * 订阅用户ID
     */
    private volatile String userId;

    /**
     * 订阅会话属性
     */
    private volatile Map<String, String> attributes;

    /**
     * 帧回调监听器
     */
    private volatile SseFrameListener listener;

    /**
     * 构造SSE订阅器
     *
     * @param httpClient   统一HTTP客户端
     * @param config       SDK配置
     * @param tokenManager 令牌管理器
     */
    public SsePushSubscriber(IBaseHttpClient httpClient, SdkConfig config, TokenManager tokenManager) {
        this.httpClient = httpClient;
        this.config = config;
        this.tokenManager = tokenManager;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "tbs-sdk-sse");
            thread.setDaemon(true);
            return thread;
        });
        this.sseClient = new OkHttpClient.Builder()
            .connectTimeout(config.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(List<String> topics, String userId, Map<String, String> attributes,
                          SseFrameListener listener) {
        if (listener == null) {
            throw new SdkClientException("listener不能为空");
        }
        this.listener = listener;
        this.userId = userId;
        this.attributes = attributes;
        this.topics = topics == null ? new ArrayList<>() : new ArrayList<>(topics);
        connectAsync();
    }

    /**
     * 异步发起建连全流程
     */
    private void connectAsync() {
        scheduler.execute(this::connect);
    }

    /**
     * 建连全流程：认证签发ticket → EventSource建SSE下行流
     */
    private void connect() {
        if (closed.get()) {
            return;
        }
        PushCredential credential;
        try {
            credential = authenticate();
        } catch (Exception e) {
            handleConnectionFailure(e);
            return;
        }
        sessionIdRef.set(credential.getSessionId());
        String url = buildConnectUrl(credential.getTicket());
        Request request = new Request.Builder().url(url).get().build();
        cancelCurrentEventSource();
        eventSource = EventSources.createFactory(sseClient).newEventSource(request, new FrameEventListener());
    }

    /**
     * 调用认证端点签发一次性建连凭证
     *
     * @return 连接凭证
     * @throws SdkAuthException   未登录或令牌失效时抛出
     * @throws SdkClientException 网络失败或响应非法时抛出
     */
    private PushCredential authenticate() {
        if (!tokenManager.hasToken()) {
            throw new SdkAuthException(SdkConstants.CODE_UNAUTHORIZED, "未登录，无法建立推送连接");
        }
        PushAuthForm form = new PushAuthForm();
        form.setToken(tokenManager.peek().getToken());
        form.setUserId(userId);
        form.setAttributes(attributes);
        return httpClient.post(SdkConstants.PATH_PUSH_AUTH, form, PushCredential.class);
    }

    /**
     * 拼接SSE建连URL（ticket经query param携带）
     *
     * @param ticket 一次性连接凭证
     * @return 完整建连URL
     */
    private String buildConnectUrl(String ticket) {
        String base = config.getBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + SdkConstants.PATH_PUSH_SSE_CONNECT
            + "?" + SdkConstants.PARAM_PUSH_TICKET + "=" + ticket;
    }

    /**
     * 帧事件监听器：解析下行帧并分发回调，握手成功后启动订阅与心跳
     */
    private class FrameEventListener extends EventSourceListener {

        /**
         * 流建立成功回调（仅记录，业务握手以hello帧为准）
         *
         * @param eventSource 事件流
         * @param response    HTTP响应
         */
        @Override
        public void onOpen(EventSource eventSource, Response response) {
            log.debug("SSE连接已建立(HTTP {})", response.code());
        }

        /**
         * 收到SSE事件：解析协议帧并按类型分发
         *
         * @param eventSource 事件流
         * @param id          事件ID
         * @param type        事件名（兼容带push-前缀与裸小写值两种形态）
         * @param data        帧JSON文本
         */
        @Override
        public void onEvent(EventSource eventSource, String id, String type, String data) {
            PushFrame frame = parseFrame(data);
            if (frame == null || frame.getType() == null) {
                log.warn("SSE收到无法解析的帧: event={}, data={}", type, data);
                return;
            }
            if (frame.getType() == FrameType.HELLO) {
                onHandshake();
            }
            SseFrameListener current = listener;
            if (current != null) {
                dispatch(() -> current.onFrame(frame.getType(), frame));
            }
        }

        /**
         * 流异常终止：按重连策略处理
         *
         * @param eventSource 事件流
         * @param t           异常
         * @param response    服务端响应（可为null）
         */
        @Override
        public void onFailure(EventSource eventSource, Throwable t, Response response) {
            if (closed.get()) {
                return;
            }
            handleConnectionFailure(t != null ? t
                : new SdkClientException("SSE连接失败(HTTP " + (response != null ? response.code() : -1) + ")"));
        }

        /**
         * 服务端优雅关闭流：按重连策略处理
         *
         * @param eventSource 事件流
         */
        @Override
        public void onClosed(EventSource eventSource) {
            if (closed.get()) {
                return;
            }
            handleConnectionFailure(new SdkClientException("服务端已关闭SSE连接"));
        }

        /**
         * 解析帧JSON
         *
         * @param data 帧文本
         * @return 协议帧，解析失败为null
         */
        private PushFrame parseFrame(String data) {
            if (data == null || data.isEmpty()) {
                return null;
            }
            try {
                return mapper.readValue(data, PushFrame.class);
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * 握手成功处理：清零重连计数、启动心跳、回调连接建立并上行初始订阅
     */
    private void onHandshake() {
        reconnectAttempts.set(0);
        startPing();
        SseFrameListener current = listener;
        String sessionId = sessionIdRef.get();
        if (current != null && sessionId != null) {
            dispatch(() -> current.onConnected(sessionId));
        }
        for (String topic : new ArrayList<>(topics)) {
            uploadTopicFrame(FrameType.SUBSCRIBE, topic);
        }
    }

    /**
     * 连接失败统一处理：按指数退避重连或终态回调
     *
     * @param cause 失败原因
     */
    private void handleConnectionFailure(Throwable cause) {
        cancelPing();
        sessionIdRef.set(null);
        if (!config.isSseReconnectEnabled()) {
            log.debug("SSE连接失败且未启用重连", cause);
            SseFrameListener current = listener;
            if (current != null) {
                dispatch(() -> current.onError(cause));
            }
            return;
        }
        int attempts = reconnectAttempts.incrementAndGet();
        if (attempts > config.getSseMaxReconnectAttempts()) {
            SseFrameListener current = listener;
            if (current != null) {
                dispatch(() -> current.onError(new SdkClientException(
                    "SSE重连次数耗尽(" + config.getSseMaxReconnectAttempts() + ")", cause)));
            }
            return;
        }
        long delay = config.getSseReconnectBaseIntervalSeconds() * (1L << (attempts - 1));
        log.info("SSE连接失败，{}秒后进行第{}次重连: {}", delay, attempts, cause.getMessage());
        scheduler.schedule(this::connect, delay, TimeUnit.SECONDS);
    }

    /**
     * 启动心跳定时上行PING帧
     */
    private void startPing() {
        cancelPing();
        pingFuture = scheduler.scheduleAtFixedRate(this::sendPingSafe,
            PING_INTERVAL_SECONDS, PING_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 安全发送心跳（失败仅记录，交由断线检测触发重连）
     */
    private void sendPingSafe() {
        try {
            sendPing();
        } catch (Exception e) {
            log.debug("心跳上行失败: {}", e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeTopic(String topic) {
        validateTopic(topic);
        synchronized (topics) {
            if (!topics.contains(topic)) {
                topics.add(topic);
            }
        }
        uploadTopicFrame(FrameType.SUBSCRIBE, topic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeTopic(String topic) {
        validateTopic(topic);
        synchronized (topics) {
            topics.remove(topic);
        }
        uploadTopicFrame(FrameType.UNSUBSCRIBE, topic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPing() {
        uploadFrame(buildFrame(FrameType.PING, null, null));
    }

    /**
     * 上行主题相关帧（SUBSCRIBE/UNSUBSCRIBE）
     *
     * @param type  帧类型
     * @param topic 主题
     */
    private void uploadTopicFrame(FrameType type, String topic) {
        uploadFrame(buildFrame(type, topic, null));
    }

    /**
     * 构造协议帧
     *
     * @param type    帧类型
     * @param topic   主题（可为null）
     * @param payload 载荷（可为null）
     * @return 协议帧
     */
    private PushFrame buildFrame(FrameType type, String topic, String payload) {
        PushFrame frame = new PushFrame();
        frame.setVersion(SdkConstants.SSE_PROTOCOL_VERSION);
        frame.setType(type);
        frame.setTopic(topic);
        frame.setPayload(payload);
        frame.setTimestamp(System.currentTimeMillis());
        return frame;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uploadFrame(PushFrame frame) {
        String sessionId = sessionIdRef.get();
        if (sessionId == null) {
            throw new SdkClientException("尚未建立SSE会话，无法上行帧");
        }
        httpClient.post(SdkConstants.PATH_PUSH_SSE_UPLOAD, FrameUploadRequest.of(sessionId, frame), Object.class);
    }

    /**
     * 校验主题合法性
     *
     * @param topic 主题
     */
    private void validateTopic(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            throw new SdkClientException("topic不能为空");
        }
        if (topic.length() > SdkConstants.TOPIC_MAX_LENGTH) {
            throw new SdkClientException("topic长度不能超过" + SdkConstants.TOPIC_MAX_LENGTH + "字符");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String currentSessionId() {
        return sessionIdRef.get();
    }

    /**
     * 在分发线程执行回调并隔离异常
     *
     * @param runnable 回调任务
     */
    private void dispatch(Runnable runnable) {
        scheduler.execute(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.warn("SSE回调执行异常(已隔离): {}", e.getMessage());
            }
        });
    }

    /**
     * 取消当前事件流（幂等）
     */
    private void cancelCurrentEventSource() {
        EventSource current = eventSource;
        if (current != null) {
            current.cancel();
        }
    }

    /**
     * 取消心跳任务（幂等）
     */
    private void cancelPing() {
        ScheduledFuture<?> future = pingFuture;
        if (future != null) {
            future.cancel(false);
            pingFuture = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        cancelPing();
        cancelCurrentEventSource();
        scheduler.shutdownNow();
    }
}
