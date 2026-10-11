package com.tbs.sdk.push;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.sdk.auth.TokenManager;
import com.tbs.sdk.config.PushOptions;
import com.tbs.sdk.config.SdkConfig;
import com.tbs.sdk.constants.SdkConstants;
import com.tbs.sdk.exception.SdkClientException;
import com.tbs.sdk.http.IBaseHttpClient;
import com.tbs.sdk.model.FrameType;
import com.tbs.sdk.model.FrameUploadRequest;
import com.tbs.sdk.model.HelloInfo;
import com.tbs.sdk.model.PushAuthForm;
import com.tbs.sdk.model.PushCredential;
import com.tbs.sdk.model.PushFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
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
 * 全流程对齐C#端：HTTP认证签发一次性push-ticket（TTL 60秒，每次建连重签，禁止复用）→
 * OkHttp EventSource携ticket作query与同源Authorization令牌头建立下行流 → 收到hello帧
 * 从其payload解析sessionId（以握手为准，非签票预分配值）→ 登记主题统一上行SUBSCRIBE →
 * 后台定时上行PING心跳 → 断线（含服务端error帧/关闭流）换票重连，退避为指数退避叠加随机抖动
 * （防惊群，1秒起上限30秒）。连接就绪前动态追加的主题仅登记，待hello后随全量主题统一订阅。
 * 帧上行采用fire-and-forget（独立线程池），失败仅告警不阻断；回调在单线程执行器中有序分发且异常隔离。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SsePushSubscriber implements ISsePushSubscriber {

    /**
     * 指数退避移位上限（防 long 溢出，实际值会被max封顶）
     */
    private static final int MAX_BACKOFF_SHIFT = 30;

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
     * SSE专用OkHttp客户端（读超时必须为0，避免空闲长连接被读超时切断）
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
     * 推送参数（心跳/退避/主题前缀/协议版本）
     */
    private final PushOptions pushOptions;

    /**
     * 令牌管理器
     */
    private final TokenManager tokenManager;

    /**
     * 连接、回调分发与重连调度执行器（单线程保证回调有序）
     */
    private final ScheduledExecutorService scheduler;

    /**
     * 帧上行专用线程池（fire-and-forget，不阻塞连接调度线程）
     */
    private final ExecutorService uploadExecutor;

    /**
     * 主题列表锁
     */
    private final Object topicsLock = new Object();

    /**
     * 已登记主题（有序去重）
     */
    private final List<String> topics = new ArrayList<>();

    /**
     * 会话标识（hello握手成功后写入，为上行帧唯一权威来源）
     */
    private final AtomicReference<String> sessionIdRef = new AtomicReference<>();

    /**
     * 关闭标志（true表示已主动close或尚未启动，建连各环节据此终止）
     */
    private final AtomicBoolean closed = new AtomicBoolean(true);

    /**
     * 连续重连尝试次数（握手成功后清零）
     */
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    /**
     * 当前连接状态
     */
    private volatile ConnectionStatus status = ConnectionStatus.IDLE;

    /**
     * 回调监听器
     */
    private volatile SseFrameListener listener;

    /**
     * 订阅用户ID
     */
    private volatile String userId;

    /**
     * 订阅会话属性
     */
    private volatile Map<String, String> attributes;

    /**
     * 当前登录令牌（subscribe时固化，每次建连刷新）
     */
    private volatile String token;

    /**
     * 当前SSE事件流
     */
    private volatile EventSource eventSource;

    /**
     * 心跳定时任务句柄
     */
    private volatile ScheduledFuture<?> pingFuture;

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
        this.pushOptions = config.getPush();
        this.tokenManager = tokenManager;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(daemonFactory("tbs-sdk-sse"));
        this.uploadExecutor = Executors.newCachedThreadPool(daemonFactory("tbs-sdk-sse-upload"));
        this.sseClient = new OkHttpClient.Builder()
            .connectTimeout(config.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();
    }

    /**
     * 构建守护线程工厂（线程不阻止JVM退出）
     *
     * @param name 线程名
     * @return 线程工厂
     */
    private static ThreadFactory daemonFactory(String name) {
        return runnable -> {
            Thread thread = new Thread(runnable, name);
            thread.setDaemon(true);
            return thread;
        };
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
        if (!tokenManager.hasToken()) {
            throw new SdkClientException("未登录，无可用令牌；请先登录再建立推送连接");
        }
        this.token = tokenManager.peek().getToken();
        synchronized (topicsLock) {
            this.topics.clear();
            this.topics.addAll(resolveDefaultTopics(topics, userId));
        }
        this.closed.set(false);
        this.reconnectAttempts.set(0);
        scheduler.execute(this::connect);
    }

    /**
     * 建连单次尝试：认证签票 → EventSource建下行流（在调度线程执行，auth为阻塞调用）
     */
    private void connect() {
        if (closed.get()) {
            return;
        }
        setStatus(reconnectAttempts.get() > 0 ? ConnectionStatus.RECONNECTING : ConnectionStatus.CONNECTING);
        // 每次建连刷新令牌（可能已随宿主重新登录而轮换）
        if (tokenManager.hasToken()) {
            this.token = tokenManager.peek().getToken();
        }
        PushCredential credential;
        try {
            credential = authenticate();
        } catch (Exception e) {
            handleFailure(e);
            return;
        }
        if (closed.get()) {
            return;
        }
        if (credential == null || credential.getTicket() == null || credential.getTicket().isEmpty()) {
            handleFailure(new SdkClientException("推送认证未返回有效ticket"));
            return;
        }
        Request request = new Request.Builder()
            .url(buildConnectUrl(credential.getTicket()))
            .header(config.getTokenHeaderName(), token)
            .get()
            .build();
        sessionIdRef.set(null);
        cancelCurrentEventSource();
        eventSource = EventSources.createFactory(sseClient).newEventSource(request, new FrameEventListener());
    }

    /**
     * 调用认证端点签发一次性建连凭证
     *
     * @return 连接凭证
     */
    private PushCredential authenticate() {
        PushAuthForm form = new PushAuthForm();
        form.setToken(token);
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
     * 下行帧事件监听器：按SSE事件名（回退帧type）路由，hello取sessionId并触发订阅/心跳/兜底信令
     */
    private class FrameEventListener extends EventSourceListener {

        /**
         * 流建立成功（仅记录，业务就绪以hello帧为准）
         *
         * @param eventSource 事件流
         * @param response    HTTP响应
         */
        @Override
        public void onOpen(EventSource eventSource, Response response) {
            log.debug("SSE下行流已建立(HTTP {})", response.code());
        }

        /**
         * 收到SSE事件：解析帧并按事件类型路由
         *
         * @param eventSource 事件流
         * @param id          事件ID
         * @param type        SSE事件名（小写值，兼容带push-前缀形态）
         * @param data        帧JSON文本
         */
        @Override
        public void onEvent(EventSource eventSource, String id, String type, String data) {
            if (closed.get()) {
                return;
            }
            PushFrame frame = parseFrame(data);
            FrameType resolved = resolveType(type, frame);
            if (resolved == FrameType.HELLO) {
                handleHello(frame);
                return;
            }
            if (resolved == FrameType.ERROR) {
                // 服务端error帧：ticket已消费，取消当前流并换票重连
                String detail = frame != null ? frame.getPayload() : data;
                cancelCurrentEventSource();
                handleFailure(new SdkClientException("收到SSE error帧: " + detail));
                return;
            }
            SseFrameListener current = listener;
            if (current == null) {
                return;
            }
            if (resolved == FrameType.SIGNAL) {
                dispatch(current::onSignal);
            }
            if (frame != null) {
                dispatch(() -> current.onFrame(resolved, frame));
            }
        }

        /**
         * 流异常终止：换票重连
         *
         * @param eventSource 事件流
         * @param t           异常（可为null）
         * @param response    服务端响应（可为null）
         */
        @Override
        public void onFailure(EventSource eventSource, Throwable t, Response response) {
            if (closed.get()) {
                return;
            }
            Throwable cause = t != null ? t
                : new SdkClientException("SSE连接失败(HTTP " + (response != null ? response.code() : -1) + ")");
            handleFailure(cause);
        }

        /**
         * 服务端优雅关闭流：视为连接异常以触发换票重连
         *
         * @param eventSource 事件流
         */
        @Override
        public void onClosed(EventSource eventSource) {
            if (closed.get()) {
                return;
            }
            handleFailure(new SdkClientException("服务端已关闭SSE连接"));
        }
    }

    /**
     * hello握手处理：解析payload取sessionId → 统一订阅登记主题 → 启动心跳 → 状态置CONNECTED →
     * 回调onHello并触发一次兜底onSignal
     *
     * @param frame hello帧
     */
    private void handleHello(PushFrame frame) {
        HelloInfo hello = frame == null ? null : parseHello(frame.getPayload());
        if (hello == null || hello.getSessionId() == null || hello.getSessionId().isEmpty()) {
            log.warn("hello帧解析失败，无法获取sessionId，忽略本次握手");
            return;
        }
        sessionIdRef.set(hello.getSessionId());
        reconnectAttempts.set(0);
        subscribeAllRegistered();
        startPing();
        setStatus(ConnectionStatus.CONNECTED);
        SseFrameListener current = listener;
        if (current != null) {
            dispatch(() -> current.onHello(hello));
            dispatch(current::onSignal);
        }
    }

    /**
     * 解析hello帧payload（JSON字符串二次解析）提取会话标识与协议版本
     *
     * @param rawPayload 帧payload原文
     * @return 握手信息，解析失败返回null
     */
    private HelloInfo parseHello(String rawPayload) {
        if (rawPayload == null || rawPayload.isEmpty()) {
            return null;
        }
        try {
            JsonNode root = mapper.readTree(rawPayload);
            if (root == null || !root.isObject()) {
                return null;
            }
            JsonNode sessionNode = root.get("sessionId");
            String sessionId = sessionNode != null && sessionNode.isTextual() ? sessionNode.textValue() : null;
            if (sessionId == null || sessionId.isEmpty()) {
                return null;
            }
            JsonNode versionNode = root.get("version");
            Integer version = versionNode != null && versionNode.isNumber() ? versionNode.intValue() : null;
            return new HelloInfo(sessionId, version);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析下行帧JSON
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

    /**
     * 归一化帧类型：优先按SSE事件名（剥去push-前缀后匹配枚举value）匹配，回退帧体type
     *
     * @param sseEvent SSE事件名
     * @param frame    已解析帧（可为null）
     * @return 帧类型，无法判定时为null
     */
    private FrameType resolveType(String sseEvent, PushFrame frame) {
        if (sseEvent != null) {
            String name = sseEvent.toLowerCase(Locale.ROOT);
            if (name.startsWith(SdkConstants.SSE_EVENT_PREFIX)) {
                name = name.substring(SdkConstants.SSE_EVENT_PREFIX.length());
            }
            for (FrameType candidate : FrameType.values()) {
                if (candidate.getValue().equals(name)) {
                    return candidate;
                }
            }
        }
        return frame == null ? null : frame.getType();
    }

    /**
     * 连接失败统一处理：分发onError后按重连策略换票退避重连，或（禁用重连时）终态关闭
     *
     * @param cause 失败原因
     */
    private void handleFailure(Throwable cause) {
        cancelPing();
        sessionIdRef.set(null);
        if (closed.get()) {
            return;
        }
        log.warn("SSE连接异常: {}", cause.getMessage());
        SseFrameListener current = listener;
        if (current != null) {
            dispatch(() -> current.onError(cause));
        }
        if (!pushOptions.isReconnectEnabled()) {
            setStatus(ConnectionStatus.CLOSED);
            return;
        }
        long delay = computeReconnectDelay();
        reconnectAttempts.incrementAndGet();
        setStatus(ConnectionStatus.RECONNECTING);
        log.info("SSE将在{}ms后进行第{}次换票重连", delay, reconnectAttempts.get());
        scheduler.schedule(this::connect, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 计算下次重连延时（指数退避封顶 + 随机抖动）
     *
     * @return 延时（毫秒）
     */
    private long computeReconnectDelay() {
        long delay = computeBaseReconnectDelayMs(
            pushOptions.getReconnectBaseDelaySeconds() * 1000L,
            pushOptions.getReconnectMaxDelaySeconds() * 1000L,
            reconnectAttempts.get());
        long jitter = pushOptions.getReconnectJitterSeconds() * 1000L;
        if (jitter > 0) {
            delay += ThreadLocalRandom.current().nextLong(jitter);
        }
        return delay;
    }

    /**
     * 计算指数退避基础延时（不含抖动），封顶maxMs。attempts自0起（0→base，1→2base...）。
     * 独立暴露为纯函数便于自检断言。
     *
     * @param baseMs   退避基数（毫秒）
     * @param maxMs    退避上限（毫秒）
     * @param attempts 已尝试次数（0起）
     * @return 基础延时（毫秒）
     */
    public static long computeBaseReconnectDelayMs(long baseMs, long maxMs, int attempts) {
        long cappedAttempts = Math.min(Math.max(attempts, 0), MAX_BACKOFF_SHIFT);
        long exponential = baseMs * (1L << cappedAttempts);
        return Math.max(0L, Math.min(maxMs, exponential));
    }

    /**
     * 动态追加订阅：就绪时立即上行，未就绪仅登记
     *
     * @param topicsToAdd 主题
     */
    @Override
    public void subscribeTopics(String... topicsToAdd) {
        List<String> normalized = normalizeTopics(topicsToAdd);
        if (normalized.isEmpty()) {
            return;
        }
        List<String> added = new ArrayList<>();
        synchronized (topicsLock) {
            for (String topic : normalized) {
                if (!topics.contains(topic)) {
                    topics.add(topic);
                    added.add(topic);
                }
            }
        }
        if (status == ConnectionStatus.CONNECTED && sessionIdRef.get() != null) {
            for (String topic : added) {
                uploadAsync(buildFrame(FrameType.SUBSCRIBE, topic, null));
            }
        }
    }

    /**
     * 动态移除订阅：就绪时同步上行退订帧
     *
     * @param topicsToRemove 主题
     */
    @Override
    public void unsubscribeTopics(String... topicsToRemove) {
        List<String> normalized = normalizeTopics(topicsToRemove);
        if (normalized.isEmpty()) {
            return;
        }
        List<String> removed = new ArrayList<>();
        synchronized (topicsLock) {
            for (String topic : normalized) {
                if (topics.remove(topic)) {
                    removed.add(topic);
                }
            }
        }
        if (status == ConnectionStatus.CONNECTED && sessionIdRef.get() != null) {
            for (String topic : removed) {
                uploadAsync(buildFrame(FrameType.UNSUBSCRIBE, topic, null));
            }
        }
    }

    /**
     * hello握手后统一订阅全部已登记主题
     */
    private void subscribeAllRegistered() {
        List<String> snapshot;
        synchronized (topicsLock) {
            snapshot = new ArrayList<>(topics);
        }
        for (String topic : snapshot) {
            uploadAsync(buildFrame(FrameType.SUBSCRIBE, topic, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPing() {
        if (sessionIdRef.get() == null) {
            return;
        }
        uploadAsync(buildFrame(FrameType.PING, null, null));
    }

    /**
     * 启动心跳定时上行PING帧（周期取自配置，<=0关闭）
     */
    private void startPing() {
        cancelPing();
        int interval = pushOptions.getHeartbeatIntervalSeconds();
        if (interval <= 0) {
            return;
        }
        pingFuture = scheduler.scheduleAtFixedRate(this::sendPing, interval, interval, TimeUnit.SECONDS);
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
    public void uploadFrame(PushFrame frame) {
        String sessionId = sessionIdRef.get();
        if (sessionId == null) {
            throw new SdkClientException("尚未建立SSE会话，无法上行帧");
        }
        fillFrameDefaults(frame);
        httpClient.post(SdkConstants.PATH_PUSH_SSE_UPLOAD, FrameUploadRequest.of(sessionId, frame), Object.class);
    }

    /**
     * fire-and-forget上行帧：会话未就绪则跳过，失败仅告警不阻断
     *
     * @param frame 协议帧
     */
    private void uploadAsync(PushFrame frame) {
        String sessionId = sessionIdRef.get();
        if (sessionId == null) {
            return;
        }
        fillFrameDefaults(frame);
        uploadExecutor.execute(() -> {
            try {
                httpClient.post(SdkConstants.PATH_PUSH_SSE_UPLOAD, FrameUploadRequest.of(sessionId, frame), Object.class);
            } catch (Exception e) {
                log.warn("帧上行失败 type={} topic={}: {}", frame.getType(), frame.getTopic(), e.getMessage());
            }
        });
    }

    /**
     * 补齐帧默认字段（协议版本、时间戳）
     *
     * @param frame 协议帧
     */
    private void fillFrameDefaults(PushFrame frame) {
        if (frame.getVersion() <= 0) {
            frame.setVersion(pushOptions.getProtocolVersion());
        }
        if (frame.getTimestamp() <= 0) {
            frame.setTimestamp(System.currentTimeMillis());
        }
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
        frame.setVersion(pushOptions.getProtocolVersion());
        frame.setType(type);
        frame.setTopic(topic);
        frame.setPayload(payload);
        frame.setTimestamp(System.currentTimeMillis());
        return frame;
    }

    /**
     * 归一化主题入参（trim并剔除空白项，不做缺省回退）
     *
     * @param input 原始主题数组
     * @return 合法主题列表
     */
    private List<String> normalizeTopics(String... input) {
        List<String> valid = new ArrayList<>();
        if (input != null) {
            for (String topic : input) {
                if (topic != null && !topic.trim().isEmpty()) {
                    String trimmed = topic.trim();
                    validateTopic(trimmed);
                    valid.add(trimmed);
                }
            }
        }
        return valid;
    }

    /**
     * 解析订阅缺省主题：入参为空时按per-user频道（topicPrefix + userId）派生
     *
     * @param input  原始主题列表
     * @param userId 用户ID
     * @return 合法主题列表
     */
    private List<String> resolveDefaultTopics(List<String> input, String userId) {
        List<String> valid = new ArrayList<>();
        if (input != null) {
            for (String topic : input) {
                if (topic != null && !topic.trim().isEmpty()) {
                    String trimmed = topic.trim();
                    validateTopic(trimmed);
                    valid.add(trimmed);
                }
            }
        }
        if (!valid.isEmpty()) {
            return valid;
        }
        if (userId != null && !userId.trim().isEmpty()) {
            valid.add(pushOptions.getTopicPrefix() + userId.trim());
        }
        return valid;
    }

    /**
     * 校验主题合法性
     *
     * @param topic 主题
     */
    private void validateTopic(String topic) {
        if (topic.length() > SdkConstants.TOPIC_MAX_LENGTH) {
            throw new SdkClientException("topic长度不能超过" + SdkConstants.TOPIC_MAX_LENGTH + "字符");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionStatus currentStatus() {
        return status;
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
     * 更新连接状态并在变更时分发回调
     *
     * @param next 新状态
     */
    private void setStatus(ConnectionStatus next) {
        if (status == next) {
            return;
        }
        status = next;
        SseFrameListener current = listener;
        if (current != null) {
            dispatch(() -> current.onStatusChanged(next));
        }
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
     * {@inheritDoc}
     */
    @Override
    public void close() {
        closed.set(true);
        cancelPing();
        cancelCurrentEventSource();
        sessionIdRef.set(null);
        reconnectAttempts.set(0);
        setStatus(ConnectionStatus.CLOSED);
    }
}
