package com.tbs.updater.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tbs.updater.exception.UpdateException;
import com.tbs.updater.model.UpdateInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 检查更新组件
 * <p>
 * 负责向检查更新接口发起POST请求并解析响应，仅依赖JDK的HttpURLConnection与Jackson。
 * 无共享可变状态，可安全复用。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class UpdateChecker {

    /**
     * 业务成功状态码
     */
    private static final int SUCCESS_CODE = 200;

    /**
     * 请求内容类型
     */
    private static final String CONTENT_TYPE = "application/json; charset=utf-8";

    /**
     * JSON处理器（线程安全，可全局复用）
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 检查更新
     * <p>
     * 以JSON形式提交appKey、platform、currentVersionCode，
     * HTTP状态非200或业务code非200均视为服务端错误。
     * </p>
     *
     * @param checkUrl           检查更新接口完整URL
     * @param appKey             应用唯一标识
     * @param platform           平台标识
     * @param currentVersionCode 当前应用版本号
     * @param timeoutSeconds     连接与读取超时时间（秒）
     * @return 服务端返回的更新信息
     * @throws UpdateException 网络异常抛NETWORK_ERROR，HTTP/业务错误抛SERVER_ERROR，解析失败抛PARSE_ERROR
     */
    public UpdateInfo check(String checkUrl, String appKey, String platform,
                            int currentVersionCode, int timeoutSeconds) throws UpdateException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(checkUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(timeoutSeconds * 1000);
            connection.setReadTimeout(timeoutSeconds * 1000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", CONTENT_TYPE);
            connection.setRequestProperty("Accept", "application/json");
            byte[] body = buildRequestBody(appKey, platform, currentVersionCode);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body);
                out.flush();
            }
            int httpStatus = connection.getResponseCode();
            if (httpStatus != HttpURLConnection.HTTP_OK) {
                throw new UpdateException(UpdateException.ErrorType.SERVER_ERROR,
                        "检查更新接口HTTP状态异常: " + httpStatus);
            }
            String responseText = readResponseBody(connection);
            return parseResponse(responseText);
        } catch (UpdateException e) {
            throw e;
        } catch (IOException e) {
            throw new UpdateException(UpdateException.ErrorType.NETWORK_ERROR,
                    "检查更新网络请求失败: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 构建检查更新请求体JSON
     *
     * @param appKey             应用唯一标识
     * @param platform           平台标识
     * @param currentVersionCode 当前应用版本号
     * @return 请求体字节数组
     * @throws UpdateException JSON序列化失败时抛出PARSE_ERROR
     */
    private byte[] buildRequestBody(String appKey, String platform, int currentVersionCode) throws UpdateException {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("appKey", appKey);
        root.put("platform", platform);
        root.put("currentVersionCode", currentVersionCode);
        try {
            return MAPPER.writeValueAsBytes(root);
        } catch (IOException e) {
            throw new UpdateException(UpdateException.ErrorType.PARSE_ERROR, "构建检查更新请求体失败", e);
        }
    }

    /**
     * 解析检查更新响应
     * <p>
     * 校验业务code为200后提取data节点交由{@link UpdateInfo#parseFromJson(JsonNode)}解析。
     * </p>
     *
     * @param responseText 响应体文本
     * @return 更新信息实例
     * @throws UpdateException JSON非法抛PARSE_ERROR，业务code非200抛SERVER_ERROR
     */
    private UpdateInfo parseResponse(String responseText) throws UpdateException {
        JsonNode root;
        try {
            root = MAPPER.readTree(responseText);
        } catch (IOException e) {
            throw new UpdateException(UpdateException.ErrorType.PARSE_ERROR, "检查更新响应JSON解析失败", e);
        }
        if (root == null || !root.isObject()) {
            throw new UpdateException(UpdateException.ErrorType.PARSE_ERROR, "检查更新响应JSON格式非法");
        }
        int code = root.path("code").asInt(-1);
        if (code != SUCCESS_CODE) {
            String message = root.path("message").asText("");
            throw new UpdateException(UpdateException.ErrorType.SERVER_ERROR,
                    "检查更新业务错误[code=" + code + "]: " + message);
        }
        return UpdateInfo.parseFromJson(root.get("data"));
    }

    /**
     * 读取响应体为UTF-8文本
     *
     * @param connection HTTP连接
     * @return 响应体文本
     * @throws IOException 读取失败时抛出
     */
    private String readResponseBody(HttpURLConnection connection) throws IOException {
        try (InputStream in = connection.getInputStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] chunk = new byte[4096];
            int len;
            while ((len = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, len);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
