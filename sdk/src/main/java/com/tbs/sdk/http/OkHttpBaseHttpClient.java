package com.tbs.sdk.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
 * 单例连接池复用、超时按配置初始化；令牌经拦截器从TokenManager读取并注入
 * 请求头（未登录时不注入，由服务端返回认证错误码统一裁决）。
 * 全部JSON响应统一走AjaxResult解析与业务码异常映射，下载走流式落盘。
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
     * OkHttp客户端
     */
    private final OkHttpClient client;

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
     * 构造HTTP客户端
     *
     * @param config       SDK配置
     * @param tokenManager 令牌管理器
     */
    public OkHttpBaseHttpClient(SdkConfig config, TokenManager tokenManager) {
        this.config = config;
        this.tokenManager = tokenManager;
        this.mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.client = new OkHttpClient.Builder()
            .connectTimeout(config.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
            .readTimeout(config.getReadTimeoutSeconds(), TimeUnit.SECONDS)
            .writeTimeout(config.getWriteTimeoutSeconds(), TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool())
            .retryOnConnectionFailure(true)
            .addInterceptor(tokenInterceptor())
            .build();
    }

    /**
     * 构建令牌注入拦截器
     * <p>
     * 持有令牌时向请求注入配置的请求头字段；未登录时原样放行，
     * 由服务端统一返回认证错误码裁决。
     * </p>
     *
     * @return 拦截器
     */
    private Interceptor tokenInterceptor() {
        return chain -> {
            Request original = chain.request();
            if (tokenManager.hasToken()) {
                Request withToken = original.newBuilder()
                    .header(config.getTokenHeaderName(), tokenManager.peek().getToken())
                    .build();
                return chain.proceed(withToken);
            }
            return chain.proceed(original);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(String path, Map<String, String> queryParams, Class<T> dataType) {
        return execute(buildRequest(path, queryParams, null, false), dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(String path, Map<String, String> queryParams, TypeReference<T> dataType) {
        return execute(buildRequest(path, queryParams, null, false), dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T post(String path, Object jsonBody, Class<T> dataType) {
        return execute(buildRequest(path, null, jsonBody, true), dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T post(String path, Object jsonBody, TypeReference<T> dataType) {
        return execute(buildRequest(path, null, jsonBody, true), dataType);
    }

    /**
     * 构建HTTP请求
     *
     * @param path      接口路径
     * @param params    查询参数（GET时使用）
     * @param jsonBody  请求体对象（POST时使用，可为null）
     * @param post      是否POST
     * @return OkHttp请求
     * @throws SdkClientException 路径非法或请求体序列化失败时抛出
     */
    private Request buildRequest(String path, Map<String, String> params, Object jsonBody, boolean post) {
        HttpUrl url = buildUrl(path, params);
        if (!post) {
            return new Request.Builder().url(url).get().build();
        }
        RequestBody body = RequestBody.create(toJson(jsonBody), JSON_MEDIA_TYPE);
        return new Request.Builder().url(url).post(body).build();
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
     * @return JSON文本（body为null时返回"null"占位，避免构造空body失败）
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
     * 执行请求并解析data节点
     *
     * @param request  OkHttp请求
     * @param dataType data目标类型
     * @param <T>      data类型
     * @return data反序列化结果
     * @throws SdkClientException 网络、超时或解析失败时抛出
     */
    private <T> T execute(Request request, TypeReference<T> dataType) {
        return doExecute(request, dataType).getData();
    }

    /**
     * 执行请求并解析data节点
     *
     * @param request  OkHttp请求
     * @param dataType data目标类型
     * @param <T>      data类型
     * @return data反序列化结果
     * @throws SdkClientException 网络、超时或解析失败时抛出
     */
    private <T> T execute(Request request, Class<T> dataType) {
        return doExecute(request, dataType).getData();
    }

    /**
     * 执行请求并返回data已反序列化的泛型响应载体
     * <p>
     * 两阶段流转：先解析为data为原始JSON节点的载体并完成业务码校验，
     * 再将data转换为泛型目标类型，类型约束随载体对象向后传递。
     * </p>
     *
     * @param request  OkHttp请求
     * @param dataType data目标类型
     * @param <T>      data类型
     * @return data已反序列化的统一响应载体
     * @throws SdkAuthException   业务码为401/405/468时抛出
     * @throws SdkServerException 其他非成功业务码时抛出
     * @throws SdkClientException 网络、超时或解析失败时抛出
     */
    private <T> AjaxResponse<T> doExecute(Request request, TypeReference<T> dataType) {
        AjaxResponse<JsonNode> raw = doParse(request);
        return new AjaxResponse<>(raw.getCode(), raw.getMessage(), mapper.convertValue(raw.getData(), dataType));
    }

    /**
     * 执行请求并返回data已反序列化的泛型响应载体
     *
     * @param request  OkHttp请求
     * @param dataType data目标类型
     * @param <T>      data类型
     * @return data已反序列化的统一响应载体
     * @throws SdkAuthException   业务码为401/405/468时抛出
     * @throws SdkServerException 其他非成功业务码时抛出
     * @throws SdkClientException 网络、超时或解析失败时抛出
     */
    private <T> AjaxResponse<T> doExecute(Request request, Class<T> dataType) {
        AjaxResponse<JsonNode> raw = doParse(request);
        return new AjaxResponse<>(raw.getCode(), raw.getMessage(), mapper.convertValue(raw.getData(), dataType));
    }

    /**
     * 执行请求并完成统一响应解析与业务码校验（data保持原始JSON节点）
     *
     * @param request OkHttp请求
     * @return data为原始JSON节点的统一响应载体
     * @throws SdkAuthException   业务码为401/405/468时抛出
     * @throws SdkServerException 其他非成功业务码时抛出
     * @throws SdkClientException 网络、超时或响应格式非法时抛出
     */
    private AjaxResponse<JsonNode> doParse(Request request) {
        try (Response response = client.newCall(request).execute()) {
            String bodyText = readBody(response);
            AjaxResponse<JsonNode> ajax = parseResponse(bodyText, response.code());
            SdkException error = ajax.mapToException();
            if (error != null) {
                throw error;
            }
            return ajax;
        } catch (IOException e) {
            throw new SdkClientException("网络请求失败: " + request.url().redact(), e);
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
     * 解析统一响应结构
     * <p>
     * HTTP状态非200时优先尝试按统一结构解析业务码（服务端统一包装场景），
     * 解析失败则按HTTP状态抛出服务端异常。
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
        try (Response response = client.newCall(request).execute()) {
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
