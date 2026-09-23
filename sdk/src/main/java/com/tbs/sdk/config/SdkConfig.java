package com.tbs.sdk.config;

import com.tbs.sdk.exception.SdkClientException;

/**
 * SDK配置类
 * <p>
 * 不可变配置对象，经Builder构造并完成必填校验：服务端基础地址、各环节超时、
 * 令牌请求头字段名与SSE断线重连策略。所有超时单位为秒。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SdkConfig {

    /**
     * 连接超时默认值（秒）
     */
    public static final int DEFAULT_CONNECT_TIMEOUT = 10;

    /**
     * 读取超时默认值（秒）
     */
    public static final int DEFAULT_READ_TIMEOUT = 30;

    /**
     * 写入超时默认值（秒）
     */
    public static final int DEFAULT_WRITE_TIMEOUT = 30;

    /**
     * 令牌请求头字段名默认值（与服务端WebAuthProperty默认配置一致）
     */
    public static final String DEFAULT_TOKEN_HEADER = "Authorization";

    /**
     * SSE断线重连最大次数默认值
     */
    public static final int DEFAULT_SSE_MAX_RECONNECT = 3;

    /**
     * SSE断线重连基础间隔默认值（秒）
     */
    public static final int DEFAULT_SSE_RECONNECT_INTERVAL = 2;

    /**
     * 服务端基础地址，如 https://api.example.com（不以/结尾）
     */
    private final String baseUrl;

    /**
     * 连接超时（秒）
     */
    private final int connectTimeoutSeconds;

    /**
     * 读取超时（秒）
     */
    private final int readTimeoutSeconds;

    /**
     * 写入超时（秒）
     */
    private final int writeTimeoutSeconds;

    /**
     * 令牌请求头字段名（服务端来源为header时的字段名）
     */
    private final String tokenHeaderName;

    /**
     * 是否启用SSE断线自动重连
     */
    private final boolean sseReconnectEnabled;

    /**
     * SSE断线重连最大尝试次数（超出后停止重连并回调失败）
     */
    private final int sseMaxReconnectAttempts;

    /**
     * SSE断线重连基础间隔（秒），实际间隔按尝试次数指数退避
     */
    private final int sseReconnectBaseIntervalSeconds;

    private SdkConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.connectTimeoutSeconds = builder.connectTimeoutSeconds;
        this.readTimeoutSeconds = builder.readTimeoutSeconds;
        this.writeTimeoutSeconds = builder.writeTimeoutSeconds;
        this.tokenHeaderName = builder.tokenHeaderName;
        this.sseReconnectEnabled = builder.sseReconnectEnabled;
        this.sseMaxReconnectAttempts = builder.sseMaxReconnectAttempts;
        this.sseReconnectBaseIntervalSeconds = builder.sseReconnectBaseIntervalSeconds;
    }

    /**
     * 获取服务端基础地址
     *
     * @return 基础地址
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 获取连接超时（秒）
     *
     * @return 连接超时
     */
    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    /**
     * 获取读取超时（秒）
     *
     * @return 读取超时
     */
    public int getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    /**
     * 获取写入超时（秒）
     *
     * @return 写入超时
     */
    public int getWriteTimeoutSeconds() {
        return writeTimeoutSeconds;
    }

    /**
     * 获取令牌请求头字段名
     *
     * @return 请求头字段名
     */
    public String getTokenHeaderName() {
        return tokenHeaderName;
    }

    /**
     * 是否启用SSE断线自动重连
     *
     * @return 启用返回true
     */
    public boolean isSseReconnectEnabled() {
        return sseReconnectEnabled;
    }

    /**
     * 获取SSE断线重连最大尝试次数
     *
     * @return 最大尝试次数
     */
    public int getSseMaxReconnectAttempts() {
        return sseMaxReconnectAttempts;
    }

    /**
     * 获取SSE断线重连基础间隔（秒）
     *
     * @return 基础间隔
     */
    public int getSseReconnectBaseIntervalSeconds() {
        return sseReconnectBaseIntervalSeconds;
    }

    /**
     * 创建配置构造器
     *
     * @param baseUrl 服务端基础地址（必填）
     * @return 构造器实例
     */
    public static Builder builder(String baseUrl) {
        return new Builder(baseUrl);
    }

    /**
     * 配置构造器
     */
    public static class Builder {

        /**
         * 服务端基础地址
         */
        private final String baseUrl;

        /**
         * 连接超时（秒）
         */
        private int connectTimeoutSeconds = DEFAULT_CONNECT_TIMEOUT;

        /**
         * 读取超时（秒）
         */
        private int readTimeoutSeconds = DEFAULT_READ_TIMEOUT;

        /**
         * 写入超时（秒）
         */
        private int writeTimeoutSeconds = DEFAULT_WRITE_TIMEOUT;

        /**
         * 令牌请求头字段名
         */
        private String tokenHeaderName = DEFAULT_TOKEN_HEADER;

        /**
         * 是否启用SSE断线自动重连
         */
        private boolean sseReconnectEnabled = false;

        /**
         * SSE断线重连最大尝试次数
         */
        private int sseMaxReconnectAttempts = DEFAULT_SSE_MAX_RECONNECT;

        /**
         * SSE断线重连基础间隔（秒）
         */
        private int sseReconnectBaseIntervalSeconds = DEFAULT_SSE_RECONNECT_INTERVAL;

        private Builder(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        /**
         * 设置连接超时（秒）
         *
         * @param seconds 超时秒数
         * @return 构造器
         */
        public Builder connectTimeout(int seconds) {
            this.connectTimeoutSeconds = seconds;
            return this;
        }

        /**
         * 设置读取超时（秒）
         *
         * @param seconds 超时秒数
         * @return 构造器
         */
        public Builder readTimeout(int seconds) {
            this.readTimeoutSeconds = seconds;
            return this;
        }

        /**
         * 设置写入超时（秒）
         *
         * @param seconds 超时秒数
         * @return 构造器
         */
        public Builder writeTimeout(int seconds) {
            this.writeTimeoutSeconds = seconds;
            return this;
        }

        /**
         * 设置令牌请求头字段名（默认Authorization，需与服务端令牌拾取配置一致）
         *
         * @param name 请求头字段名
         * @return 构造器
         */
        public Builder tokenHeaderName(String name) {
            this.tokenHeaderName = name;
            return this;
        }

        /**
         * 启用SSE断线自动重连
         *
         * @param maxAttempts         最大尝试次数
         * @param baseIntervalSeconds 基础间隔秒数（按尝试次数指数退避）
         * @return 构造器
         */
        public Builder sseReconnect(int maxAttempts, int baseIntervalSeconds) {
            this.sseReconnectEnabled = true;
            this.sseMaxReconnectAttempts = maxAttempts;
            this.sseReconnectBaseIntervalSeconds = baseIntervalSeconds;
            return this;
        }

        /**
         * 构造不可变配置对象
         *
         * @return 配置对象
         * @throws SdkClientException 必填项缺失或参数非法时抛出
         */
        public SdkConfig build() {
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                throw new SdkClientException("baseUrl不能为空");
            }
            if (connectTimeoutSeconds <= 0 || readTimeoutSeconds <= 0 || writeTimeoutSeconds <= 0) {
                throw new SdkClientException("超时时间必须为正数");
            }
            if (tokenHeaderName == null || tokenHeaderName.trim().isEmpty()) {
                throw new SdkClientException("tokenHeaderName不能为空");
            }
            if (sseMaxReconnectAttempts < 0) {
                throw new SdkClientException("SSE重连次数不能为负数");
            }
            if (sseReconnectBaseIntervalSeconds < 0) {
                throw new SdkClientException("SSE重连间隔不能为负数");
            }
            return new SdkConfig(this);
        }
    }
}
