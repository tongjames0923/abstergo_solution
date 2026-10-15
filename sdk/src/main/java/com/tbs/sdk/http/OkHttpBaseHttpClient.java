package com.tbs.sdk.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.sdk.auth.TokenManager;
import com.tbs.sdk.config.SdkConfig;
import com.tbs.sdk.exception.SdkClientException;
import com.tbs.sdk.exception.SdkException;
import com.tbs.sdk.exception.SdkServerException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 基于OkHttp的统一HTTP客户端实现
 * <p>
 * 单例连接池复用、超时按配置初始化；请求头经拦截器注入令牌与语言（显式令牌已由调用方
 * 设置在请求上时不覆盖，未登录时不注入令牌由服务端统一裁决）。以 {@link #send} 为泛化入口
 * 统一解析AjaxResult并按业务码判定：成功返回、会话失效（405/468）先回调监听器再抛重登异常、
 * 其余非成功码抛服务端异常；隔离型请求跳过登录态处理直接返回原始载体。下载走独立无读超时
 * 客户端流式落盘，临时文件原子重命名防半包。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class OkHttpBaseHttpClient implements IBaseHttpClient {

    /**
     * JSON内容类型
     */
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    /**
     * 下载缓冲区大小（字节）
     */
    private static final int DOWNLOAD_BUFFER_SIZE = 8192;

    /**
     * 下载临时文件后缀
     */
    private static final String TMP_FILE_SUFFIX = ".part";

    /**
     * OkHttp客户端（普通短请求）
     */
    private final OkHttpClient client;

    /**
     * 下载专用客户端（共用连接池/分发器，解除读超时限制以支撑大包长下载）
     */
    private final OkHttpClient downloadClient;

    /**
     * JSON处理器（线程安全）
     */
    private final ObjectMapper mapper;

    /**
     * 令牌管理器
     */
    private final TokenManager tokenManager;

    /**
     * SDK配置
     */
    private final SdkConfig config;

    /**
     * 会话失效监听器（volatile保证跨线程可见）
     */
    private volatile SessionExpiredListener sessionExpiredListener;

    /**
     * 构造HTTP客户端
     *
     * @param config       SDK配置
     * @param tokenManager 令牌管理器
     */
    public OkHttpBaseHttpClient(SdkConfig config, TokenManager tokenManager) {
        this.config = config;
        this.tokenManager = tokenManager;
        this.mapper = SdkObjectMappers.newMapper();
        this.client = new OkHttpClient.Builder()
            .connectTimeout(config.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
            .readTimeout(config.getReadTimeoutSeconds(), TimeUnit.SECONDS)
            .writeTimeout(config.getWriteTimeoutSeconds(), TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool())
            .retryOnConnectionFailure(true)
            .addInterceptor(headerInterceptor())
            .build();
        // 下载耗时可远超API请求，派生独立客户端解除读超时（共享连接池与分发器，close时统一释放）
        this.downloadClient = this.client.newBuilder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build();
    }

    /**
     * 构建请求头注入拦截器
     * <p>
     * 令牌：请求已由调用方显式携带时不覆盖，否则从令牌存储注入；语言：始终注入
     * Accept-Language（缺省zh_CN）。未登录且无显式令牌时原样放行由服务端裁决。
     * </p>
     *
     * @return 拦截器
     */
    private Interceptor headerInterceptor() {
        return chain -> {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();
            if (original.header(config.getTokenHeaderName()) == null && tokenManager.hasToken()) {
                builder.header(config.getTokenHeaderName(), tokenManager.peek().getToken());
            }
            if (original.header("Accept-Language") == null) {
                builder.header("Accept-Language", config.getLanguage());
            }
            return chain.proceed(builder.build());
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(String path, Map<String, String> queryParams, Class<T> dataType) {
        return data(send("GET", path, null, queryParams, null), dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(String path, Map<String, String> queryParams, TypeReference<T> dataType) {
        return data(send("GET", path, null, queryParams, null), dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(String path, Map<String, String> queryParams, Class<T> dataType, SdkRequestOptions options) {
        return data(send("GET", path, null, queryParams, options), dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T post(String path, Object jsonBody, Class<T> dataType) {
        return data(send("POST", path, jsonBody, null, null), dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T post(String path, Object jsonBody, TypeReference<T> dataType) {
        return data(send("POST", path, jsonBody, null, null), dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T post(String path, Object jsonBody, Class<T> dataType, SdkRequestOptions options) {
        return data(send("POST", path, jsonBody, null, options), dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T put(String path, Object jsonBody, Class<T> dataType) {
        return data(send("PUT", path, jsonBody, null, null), dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T delete(String path, Map<String, String> queryParams, Class<T> dataType) {
        return data(send("DELETE", path, null, queryParams, null), dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T patch(String path, Object jsonBody, Class<T> dataType) {
        return data(send("PATCH", path, jsonBody, null, null), dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AjaxResponse<JsonNode> send(String method, String path, Object jsonBody,
                                       Map<String, String> queryParams, SdkRequestOptions options) {
        Request request = buildRequest(method, path, jsonBody, queryParams, options);
        AjaxResponse<JsonNode> ajax;
        try (Response response = client.newCall(request).execute()) {
            String bodyText = readBody(response);
            ajax = parseResponse(bodyText, response.code());
        } catch (IOException e) {
            throw new SdkClientException("网络请求失败: " + request.url().redact(), e);
        }

        // 隔离型请求：跳过统一登录态处理，直接返回原始响应体由调用方自行判定
        if (options != null && options.isSkipAuthHandling()) {
            return ajax;
        }

        SdkException error = ajax.mapToException();
        if (error != null) {
            if (AjaxResponse.isSessionExpired(ajax.getCode())) {
                SessionExpiredListener listener = sessionExpiredListener;
                if (listener != null) {
                    listener.onSessionExpired(ajax.getCode(), ajax.getMessage());
                }
            }
            throw error;
        }
        return ajax;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSessionExpiredListener(SessionExpiredListener listener) {
        this.sessionExpiredListener = listener;
    }

    /**
     * 从统一响应载体提取data并转换为目标类型
     *
     * @param raw      data为原始JSON节点的响应载体
     * @param dataType 目标类型
     * @param <T>      目标类型
     * @return data反序列化结果
     */
    private <T> T data(AjaxResponse<JsonNode> raw, Class<T> dataType) {
        return mapper.convertValue(raw.getData(), dataType);
    }

    /**
     * 从统一响应载体提取data并转换为目标泛型类型
     *
     * @param raw      data为原始JSON节点的响应载体
     * @param dataType 目标类型引用
     * @param <T>      目标类型
     * @return data反序列化结果
     */
    private <T> T data(AjaxResponse<JsonNode> raw, TypeReference<T> dataType) {
        return mapper.convertValue(raw.getData(), dataType);
    }

    /**
     * 按动词构建OkHttp请求
     *
     * @param method  HTTP动词（GET/POST/PUT/DELETE/PATCH，大小写不敏感）
     * @param path    接口路径
     * @param body    请求体对象（GET/DELETE忽略）
     * @param params  查询参数（可为null）
     * @param options 请求级选项（可为null）
     * @return OkHttp请求
     * @throws SdkClientException 动词或路径非法时抛出
     */
    private Request buildRequest(String method, String path, Object body,
                                 Map<String, String> params, SdkRequestOptions options) {
        HttpUrl url = buildUrl(path, params);
        Request.Builder builder = new Request.Builder().url(url);
        String explicitToken = options == null ? null : options.getExplicitToken();
        if (explicitToken != null && !explicitToken.trim().isEmpty()) {
            builder.header(config.getTokenHeaderName(), explicitToken);
        }
        switch (method == null ? "" : method.toUpperCase(Locale.ROOT)) {
            case "GET":
                return builder.get().build();
            case "DELETE":
                return builder.delete().build();
            case "POST":
                return builder.post(RequestBody.create(toJson(body), JSON_MEDIA_TYPE)).build();
            case "PUT":
                return builder.put(RequestBody.create(toJson(body), JSON_MEDIA_TYPE)).build();
            case "PATCH":
                return builder.patch(RequestBody.create(toJson(body), JSON_MEDIA_TYPE)).build();
            default:
                throw new SdkClientException("不支持的请求方法: " + method);
        }
    }

    /**
     * 拼接完整请求URL
     *
     * @param path   接口路径
     * @param params 查询参数（可为null）
     * @return 完整URL
     * @throws SdkClientException 路径或参数非法时抛出
     */
    private HttpUrl buildUrl(String path, Map<String, String> params) {
        HttpUrl base = HttpUrl.parse(config.getBaseUrl());
        if (base == null) {
            throw new SdkClientException("baseUrl非法: " + config.getBaseUrl());
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        HttpUrl resolved = base.resolve(normalized);
        if (resolved == null) {
            throw new SdkClientException("请求路径非法: " + path);
        }
        if (params == null || params.isEmpty()) {
            return resolved;
        }
        HttpUrl.Builder builder = resolved.newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                builder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    /**
     * 序列化请求体
     *
     * @param body 请求体对象
     * @return JSON文本（body为null时返回"{}"占位，避免构造空body失败）
     * @throws SdkClientException 序列化失败时抛出
     */
    private String toJson(Object body) {
        if (body == null) {
            return "{}";
        }
        try {
            return mapper.writeValueAsString(body);
        } catch (IOException e) {
            throw new SdkClientException("请求体JSON序列化失败", e);
        }
    }

    /**
     * 读取响应体文本
     *
     * @param response OkHttp响应
     * @return 响应体文本
     * @throws IOException 读取失败时抛出
     */
    private String readBody(Response response) throws IOException {
        ResponseBody body = response.body();
        return body == null ? "" : body.string();
    }

    /**
     * 解析统一响应结构（data保持原始JSON节点）
     * <p>
     * HTTP状态非成功或响应非法时优先尝试按统一结构解析业务码（服务端统一包装场景），
     * 非JSON或缺少code字段则按HTTP状态抛出服务端异常。
     * </p>
     *
     * @param bodyText   响应体文本
     * @param httpStatus HTTP状态码
     * @return 统一响应载体
     * @throws SdkServerException 响应非法或HTTP层异常时抛出
     */
    private AjaxResponse<JsonNode> parseResponse(String bodyText, int httpStatus) {
        JsonNode root;
        try {
            root = mapper.readTree(bodyText);
        } catch (IOException e) {
            throw new SdkServerException(httpStatus, "响应JSON解析失败(HTTP " + httpStatus + ")");
        }
        if (root == null || !root.isObject() || !root.hasNonNull("code")) {
            throw new SdkServerException(httpStatus, "响应结构非法(HTTP " + httpStatus + ")");
        }
        int code = root.path("code").asInt(-1);
        String message = root.path("message").asText("");
        return new AjaxResponse(code, message, root.get("data"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void download(String url, File target, DownloadProgressListener listener) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            throw new SdkClientException("下载地址非法: " + url);
        }
        File tmp = new File(target.getParentFile(), target.getName() + TMP_FILE_SUFFIX);
        Request request = new Request.Builder().url(httpUrl).get().build();
        try (Response response = downloadClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new SdkClientException("下载HTTP状态异常: " + response.code());
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new SdkClientException("下载响应体为空");
            }
            long total = body.contentLength();
            writeStreamToFile(body.byteStream(), tmp, total, listener);
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            deleteQuietly(tmp);
            throw new SdkClientException("文件下载失败: " + url, e);
        } catch (SdkClientException e) {
            deleteQuietly(tmp);
            throw e;
        }
    }

    /**
     * 将响应流写入本地临时文件并回调进度
     *
     * @param in       响应字节流
     * @param tmp      临时文件
     * @param total    总字节数（未知为-1）
     * @param listener 进度回调（可为null）
     * @throws IOException 写入失败时抛出
     */
    private void writeStreamToFile(InputStream in, File tmp, long total,
                                   DownloadProgressListener listener) throws IOException {
        long downloaded = 0;
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                downloaded += len;
                if (listener != null) {
                    listener.onProgress(downloaded, total);
                }
            }
        }
    }

    /**
     * 静默删除文件
     *
     * @param file 目标文件
     */
    private void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException ignored) {
                /*
                 * 临时文件清理失败不影响主流程，交由操作系统回收
                 */
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
}
