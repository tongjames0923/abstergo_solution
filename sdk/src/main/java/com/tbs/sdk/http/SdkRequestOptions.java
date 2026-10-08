package com.tbs.sdk.http;

/**
 * 单次请求级选项
 * <p>
 * 对齐前端 request.js 的请求级配置：显式令牌覆盖令牌存储当前值、隔离型请求跳过统一
 * 登录态处理。为null传入各请求方法时按默认处理（从令牌存储注入令牌、走统一业务码判定）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SdkRequestOptions {

    /**
     * 显式令牌：优先于令牌存储中的当前令牌注入请求头
     * （对齐前端"请求头已携带令牌时不覆盖"，用于第三方连通性测试等场景）
     */
    private String explicitToken;

    /**
     * 隔离型请求：跳过统一登录态处理（不触发会话失效事件与重登异常），
     * 直接返回原始响应体由调用方自行判定code（对齐前端 _skipAuthHandling）
     */
    private boolean skipAuthHandling;

    public String getExplicitToken() {
        return explicitToken;
    }

    public SdkRequestOptions setExplicitToken(String explicitToken) {
        this.explicitToken = explicitToken;
        return this;
    }

    public boolean isSkipAuthHandling() {
        return skipAuthHandling;
    }

    public SdkRequestOptions setSkipAuthHandling(boolean skipAuthHandling) {
        this.skipAuthHandling = skipAuthHandling;
        return this;
    }
}
