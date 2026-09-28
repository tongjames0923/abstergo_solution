package com.tbs.sdk.http;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.Closeable;
import java.io.File;
import java.util.Map;

/**
 * 统一HTTP客户端契约
 * <p>
 * SDK内全部网络请求的唯一出口：GET/POST JSON请求统一解析服务端AjaxResult结构
 * 并按业务码映射SDK异常（认证/服务端/客户端三类），下载接口提供流式落盘与进度回调。
 * 令牌注入与超时、连接池管理由实现层负责，调用方不感知。
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
     * @throws com.tbs.sdk.exception.SdkAuthException   业务码为401/405/468时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 其他非成功业务码时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络、超时或解析失败时抛出
     */
    <T> T get(String path, Map<String, String> queryParams, TypeReference<T> dataType);

    /**
     * 发起POST JSON请求并解析data节点
     *
     * @param path     接口路径（相对baseUrl，以/开头）
     * @param jsonBody 请求体对象（Jackson序列化，可为null表示无body）
     * @param dataType data节点目标类型
     * @param <T>      data类型
     * @return data反序列化结果
     * @throws com.tbs.sdk.exception.SdkAuthException   业务码为401/405/468时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 其他非成功业务码时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络、超时或解析失败时抛出
     */
    <T> T post(String path, Object jsonBody, Class<T> dataType);

    /**
     * 发起POST JSON请求并解析data节点（泛型目标类型）
     *
     * @param path     接口路径（相对baseUrl，以/开头）
     * @param jsonBody 请求体对象（Jackson序列化，可为null表示无body）
     * @param dataType data节点目标类型引用
     * @param <T>      data类型
     * @return data反序列化结果
     * @throws com.tbs.sdk.exception.SdkAuthException   业务码为401/405/468时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 其他非成功业务码时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络、超时或解析失败时抛出
     */
    <T> T post(String path, Object jsonBody, TypeReference<T> dataType);

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
}
