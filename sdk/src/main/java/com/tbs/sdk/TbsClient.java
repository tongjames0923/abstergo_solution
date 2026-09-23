package com.tbs.sdk;

import com.tbs.sdk.auth.ILoginApi;
import com.tbs.sdk.auth.LoginApi;
import com.tbs.sdk.auth.TokenManager;
import com.tbs.sdk.config.SdkConfig;
import com.tbs.sdk.http.OkHttpBaseHttpClient;
import com.tbs.sdk.push.IPushMessageApi;
import com.tbs.sdk.push.ISsePushSubscriber;
import com.tbs.sdk.push.PushMessageApi;
import com.tbs.sdk.push.SsePushSubscriber;
import com.tbs.sdk.update.IUpdateApi;
import com.tbs.sdk.update.UpdateApi;
import java.io.Closeable;
import java.io.IOException;

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
     * 获取SDK配置
     *
     * @return 配置对象
     */
    public SdkConfig getConfig() {
        return config;
    }

    /**
     * 释放全部资源（幂等）：先关闭SSE订阅器（含其内部执行器），
     * 再关闭HTTP客户端连接池。
     *
     * @throws IOException 资源释放失败时抛出
     */
    @Override
    public void close() throws IOException {
        sse.close();
        httpClient.close();
    }
}
