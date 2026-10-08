package com.tbs.sdk;

import com.tbs.sdk.auth.ILoginApi;
import com.tbs.sdk.auth.LoginApi;
import com.tbs.sdk.auth.TokenManager;
import com.tbs.sdk.config.SdkConfig;
import com.tbs.sdk.exception.SdkClientException;
import com.tbs.sdk.http.OkHttpBaseHttpClient;
import com.tbs.sdk.http.SessionExpiredListener;
import com.tbs.sdk.push.IPushMessageApi;
import com.tbs.sdk.push.ISsePushSubscriber;
import com.tbs.sdk.push.PushMessageApi;
import com.tbs.sdk.push.SseFrameListener;
import com.tbs.sdk.push.SsePushSubscriber;
import com.tbs.sdk.update.IUpdateApi;
import com.tbs.sdk.update.UpdateApi;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * SDK顶层门面
 * <p>
 * 组合全部能域API（登录/推送消息中心/SSE订阅/应用更新），持有统一的
 * HTTP客户端与令牌管理器。构造即装配，无Spring依赖；实现Closeable，
 * 用毕释放SSE连接与OkHttp连接池。各能域以接口形式暴露，与服务端
 * "薄壳控制器→门面服务"的分层一一映射。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class TbsClient implements Closeable {

    /**
     * SDK配置
     */
    private final SdkConfig config;

    /**
     * 令牌管理器
     */
    private final TokenManager tokenManager;

    /**
     * 统一HTTP客户端实现
     */
    private final OkHttpBaseHttpClient httpClient;

    /**
     * 登录能域
     */
    private final ILoginApi login;

    /**
     * 推送消息中心能域
     */
    private final IPushMessageApi push;

    /**
     * SSE订阅能域
     */
    private final ISsePushSubscriber sse;

    /**
     * 应用更新能域
     */
    private final IUpdateApi update;

    private TbsClient(SdkConfig config) {
        this.config = config;
        this.tokenManager = new TokenManager();
        this.httpClient = new OkHttpBaseHttpClient(config, tokenManager);
        this.login = new LoginApi(httpClient, tokenManager);
        this.push = new PushMessageApi(httpClient);
        this.sse = new SsePushSubscriber(httpClient, config, tokenManager);
        this.update = new UpdateApi(httpClient);
    }

    /**
     * 以完整配置构造客户端
     *
     * @param config 配置对象（SdkConfig.builder(baseUrl)...build()）
     * @return 客户端实例
     */
    public static TbsClient of(SdkConfig config) {
        return new TbsClient(config);
    }

    /**
     * 以默认配置构造客户端（仅需服务端地址）
     *
     * @param baseUrl 服务端基础地址
     * @return 客户端实例
     */
    public static TbsClient create(String baseUrl) {
        return new TbsClient(SdkConfig.builder(baseUrl).build());
    }

    /**
     * 获取登录能域API
     *
     * @return 登录API
     */
    public ILoginApi login() {
        return login;
    }

    /**
     * 获取推送消息中心能域API
     *
     * @return 推送消息中心API
     */
    public IPushMessageApi push() {
        return push;
    }

    /**
     * 获取SSE订阅能域API
     *
     * @return SSE订阅器
     */
    public ISsePushSubscriber sse() {
        return sse;
    }

    /**
     * 获取应用更新能域API
     *
     * @return 应用更新API
     */
    public IUpdateApi update() {
        return update;
    }

    /**
     * 注册会话失效监听器（收到405/468时先行回调，宿主据此引导重新登录）
     *
     * @param listener 监听器（可为null清除）
     */
    public void onSessionExpired(SessionExpiredListener listener) {
        httpClient.setSessionExpiredListener(listener);
    }

    /**
     * 登录后启动推送连接（缺省订阅per-user频道 topicPrefix + userId）
     *
     * @param userId   当前登录用户数字id（用于派生缺省主题）
     * @param listener 推送回调监听器
     * @throws SdkClientException 尚未登录无可用令牌时抛出
     */
    public void startPush(String userId, SseFrameListener listener) {
        startPush(null, userId, listener);
    }

    /**
     * 登录后启动推送连接（显式指定订阅主题）
     *
     * @param topics   订阅主题列表（为空时按userId派生per-user缺省频道）
     * @param userId   当前登录用户数字id
     * @param listener 推送回调监听器
     * @throws SdkClientException 尚未登录无可用令牌时抛出
     */
    public void startPush(List<String> topics, String userId, SseFrameListener listener) {
        if (!tokenManager.hasToken()) {
            throw new SdkClientException("尚未登录，无可用令牌；请先登录后再启动推送");
        }
        sse.subscribe(topics, userId, null, listener);
    }

    /**
     * 登出：调用后端登出接口注销令牌，关闭推送长连接并清理本地令牌（后端失败不阻断本地清理）
     */
    public void logout() {
        try {
            login.logout();
        } finally {
            sse.close();
        }
    }

    /**
     * 获取SDK配置
     *
     * @return 配置对象
     */
    public SdkConfig getConfig() {
        return config;
    }

    /**
     * 释放资源（幂等）：关闭SSE连接（订阅器守护线程随实例回收，不阻止JVM退出），
     * 再关闭HTTP客户端连接池与分发器。
     *
     * @throws IOException 资源释放失败时抛出
     */
    @Override
    public void close() throws IOException {
        sse.close();
        httpClient.close();
    }
}
