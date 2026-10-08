package com.tbs.sdk.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Closeable;
import java.io.File;
import java.util.Map;

/**
 * 统一HTTP客户端契约
 * <p>
 * SDK内全部网络请求的唯一出口：以 {@link #send} 为泛化入口覆盖任意动词与请求级选项
 * （显式令牌、隔离型请求），并在其之上提供 GET/POST/PUT/DELETE/PATCH 的类型化便捷方法；
 * 统一解析服务端AjaxResult结构并按业务码映射SDK异常，会话失效（405/468）先行回调
 * {@link SessionExpiredListener}。令牌与语言请求头自动注入、下载流式落盘由实现层负责。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IBaseHttpClient extends Closeable {

    /**
     * 发起GET请求并解析data节点
     *
     * @param path        接口路径（相对baseUrl，以/开头）
     * @param queryParams 查询参数（可为null）
     * @param dataType    data节点目标类型
     * @param <T>         data类型
     * @return data反序列化结果
     * @throws com.tbs.sdk.exception.SdkAuthException   业务码为401/405/468时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 其他非成功业务码时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络、超时或解析失败时抛出
     */
    <T> T get(String path, Map<String, String> queryParams, Class<T> dataType);

    /**
     * 发起GET请求并解析data节点（泛型目标类型）
     *
     * @param path        接口路径（相对baseUrl，以/开头）
     * @param queryParams 查询参数（可为null）
     * @param dataType    data节点目标类型引用（如List&lt;Xxx&gt;）
     * @param <T>         data类型
     * @return data反序列化结果
     */
    <T> T get(String path, Map<String, String> queryParams, TypeReference<T> dataType);

    /**
     * 发起GET请求并解析data节点（携带请求级选项：显式令牌/隔离型请求）
     *
     * @param path        接口路径
     * @param queryParams 查询参数（可为null）
     * @param dataType    data节点目标类型
     * @param options     请求级选项（可为null按默认处理）
     * @param <T>         data类型
     * @return data反序列化结果
     */
    <T> T get(String path, Map<String, String> queryParams, Class<T> dataType, SdkRequestOptions options);

    /**
     * 发起POST JSON请求并解析data节点
     *
     * @param path     接口路径（相对baseUrl，以/开头）
     * @param jsonBody 请求体对象（Jackson序列化，可为null表示无body）
     * @param dataType data节点目标类型
     * @param <T>      data类型
     * @return data反序列化结果
     */
    <T> T post(String path, Object jsonBody, Class<T> dataType);

    /**
     * 发起POST JSON请求并解析data节点（泛型目标类型）
     *
     * @param path     接口路径
     * @param jsonBody 请求体对象（可为null）
     * @param dataType data节点目标类型引用
     * @param <T>      data类型
     * @return data反序列化结果
     */
    <T> T post(String path, Object jsonBody, TypeReference<T> dataType);

    /**
     * 发起POST JSON请求并解析data节点（携带请求级选项）
     *
     * @param path     接口路径
     * @param jsonBody 请求体对象（可为null）
     * @param dataType data节点目标类型
     * @param options  请求级选项（可为null按默认处理）
     * @param <T>      data类型
     * @return data反序列化结果
     */
    <T> T post(String path, Object jsonBody, Class<T> dataType, SdkRequestOptions options);

    /**
     * 发起PUT JSON请求并解析data节点
     *
     * @param path     接口路径
     * @param jsonBody 请求体对象（可为null）
     * @param dataType data节点目标类型
     * @param <T>      data类型
     * @return data反序列化结果
     */
    <T> T put(String path, Object jsonBody, Class<T> dataType);

    /**
     * 发起DELETE请求并解析data节点
     *
     * @param path        接口路径
     * @param queryParams 查询参数（可为null）
     * @param dataType    data节点目标类型
     * @param <T>         data类型
     * @return data反序列化结果
     */
    <T> T delete(String path, Map<String, String> queryParams, Class<T> dataType);

    /**
     * 发起PATCH JSON请求并解析data节点
     *
     * @param path     接口路径
     * @param jsonBody 请求体对象（可为null）
     * @param dataType data节点目标类型
     * @param <T>      data类型
     * @return data反序列化结果
     */
    <T> T patch(String path, Object jsonBody, Class<T> dataType);

    /**
     * 泛化请求入口：任意动词 + 请求体 + 查询参数 + 请求级选项，返回原始统一响应载体
     * <p>
     * data保持原始JSON节点，扩展字段（redirect/type/duration等）可经 {@link AjaxResponse} 读取；
     * 隔离型请求（{@link SdkRequestOptions#isSkipAuthHandling()}）跳过统一登录态处理直接返回。
     * 泛化覆盖用户/角色/部门/字典/文件等全部业务模块的自定义端点调用。
     * </p>
     *
     * @param method    HTTP动词（GET/POST/PUT/DELETE/PATCH）
     * @param path      接口路径
     * @param jsonBody  请求体对象（GET/DELETE可为null）
     * @param queryParams 查询参数（可为null）
     * @param options   请求级选项（可为null按默认处理）
     * @return data为原始JSON节点的统一响应载体
     * @throws com.tbs.sdk.exception.SdkAuthException   业务码为401/405/468时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 其他非成功业务码时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络、超时或解析失败时抛出
     */
    AjaxResponse<JsonNode> send(String method, String path, Object jsonBody,
                                Map<String, String> queryParams, SdkRequestOptions options);

    /**
     * 流式下载文件到本地
     * <p>
     * 先写入同目录临时文件（.part后缀），全部成功后原子重命名为目标文件，
     * 避免半包文件被误用；下载过程中按块回调字节进度。
     * </p>
     *
     * @param url      文件下载地址（完整URL，通常为OSS预签名地址）
     * @param target   目标文件
     * @param listener 进度回调（可为null）
     * @throws com.tbs.sdk.exception.SdkClientException 网络失败、HTTP状态异常或写入失败时抛出
     */
    void download(String url, File target, DownloadProgressListener listener);

    /**
     * 注册会话失效监听器（收到405/468时先行回调，宿主据此引导重新登录）
     *
     * @param listener 监听器（可为null清除）
     */
    void setSessionExpiredListener(SessionExpiredListener listener);
}
