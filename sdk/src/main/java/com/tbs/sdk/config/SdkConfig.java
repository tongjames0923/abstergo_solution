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
     * 语言请求头默认值（Accept-Language，与服务端 i18n 及前端 zh_CN 对齐）
     */
    public static final String DEFAULT_LANGUAGE = "zh_CN";

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
     * 语言请求头 Accept-Language 取值（zh_CN / en_US）
     */
    private final String language;

    /**
     * SSE推送参数（可变配置，集中承载主题/心跳/重连退避）
     */
    private final PushOptions push;

    private SdkConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.connectTimeoutSeconds = builder.connectTimeoutSeconds;
        this.readTimeoutSeconds = builder.readTimeoutSeconds;
        this.writeTimeoutSeconds = builder.writeTimeoutSeconds;
        this.tokenHeaderName = builder.tokenHeaderName;
        this.language = builder.language;
        this.push = builder.push;
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
     * 获取语言请求头 Accept-Language 取值
     *
     * @return 语言取值
     */
    public String getLanguage() {
        return language;
    }

    /**
     * 获取SSE推送参数（可变，宿主可覆盖心跳/重连/主题前缀等）
     *
     * @return 推送参数对象
     */
    public PushOptions getPush() {
        return push;
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
         * 语言请求头取值
         */
        private String language = DEFAULT_LANGUAGE;

        /**
         * SSE推送参数
         */
        private final PushOptions push = new PushOptions();

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
         * 设置语言请求头 Accept-Language 取值（默认zh_CN，需与服务端 i18n 支持一致）
         *
         * @param language 语言取值
         * @return 构造器
         */
        public Builder language(String language) {
            this.language = language;
            return this;
        }

        /**
         * 覆盖SSE推送参数（心跳/重连退避/主题前缀/协议版本）
         *
         * @param push 推送参数（为null时保留默认）
         * @return 构造器
         */
        public Builder push(PushOptions push) {
            if (push != null) {
                this.push.setTopicPrefix(push.getTopicPrefix())
                    .setHeartbeatIntervalSeconds(push.getHeartbeatIntervalSeconds())
                    .setReconnectEnabled(push.isReconnectEnabled())
                    .setReconnectBaseDelaySeconds(push.getReconnectBaseDelaySeconds())
                    .setReconnectMaxDelaySeconds(push.getReconnectMaxDelaySeconds())
                    .setReconnectJitterSeconds(push.getReconnectJitterSeconds())
                    .setProtocolVersion(push.getProtocolVersion());
            }
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
            if (language == null || language.trim().isEmpty()) {
                throw new SdkClientException("language不能为空");
            }
            if (push.getReconnectBaseDelaySeconds() < 0 || push.getReconnectMaxDelaySeconds() < 0
                || push.getReconnectJitterSeconds() < 0 || push.getHeartbeatIntervalSeconds() < 0) {
                throw new SdkClientException("推送心跳/重连参数不能为负数");
            }
            return new SdkConfig(this);
        }
    }
}
