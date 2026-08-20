package com.tbs.updater.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.tbs.updater.exception.UpdateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 更新信息模型
 * <p>
 * 承载检查更新接口返回的data节点内容，包括是否需要更新、最新版本信息、
 * 更新日志与下载地址列表。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class UpdateInfo {

    /**
     * 是否需要更新
     */
    private boolean needUpdate;

    /**
     * 升级动作标识（如强制/推荐/通知），服务端未返回时为null
     */
    private String action;

    /**
     * 最新版本号（服务端以字符串形式下发）
     */
    private String latestVersionCode;

    /**
     * 最新版本名称
     */
    private String latestVersionName;

    /**
     * 更新日志
     */
    private String releaseNote;

    /**
     * 下载地址列表
     */
    private List<String> downloadUrls = Collections.emptyList();

    /**
     * 从接口返回的data JSON节点解析更新信息
     * <p>
     * latestVersionCode兼容字符串与数字两种下发形态，统一转为字符串；
     * downloadUrls中的空白项会被过滤。
     * </p>
     *
     * @param dataNode 响应JSON中的data节点
     * @return 更新信息实例
     * @throws UpdateException data节点缺失或非法时抛出PARSE_ERROR
     */
    public static UpdateInfo parseFromJson(JsonNode dataNode) throws UpdateException {
        if (dataNode == null || dataNode.isNull() || !dataNode.isObject()) {
            throw new UpdateException(UpdateException.ErrorType.PARSE_ERROR, "检查更新响应缺少data字段");
        }
        UpdateInfo info = new UpdateInfo();
        info.needUpdate = dataNode.path("needUpdate").asBoolean(false);
        info.action = readText(dataNode, "action");
        info.latestVersionCode = readText(dataNode, "latestVersionCode");
        info.latestVersionName = readText(dataNode, "latestVersionName");
        info.releaseNote = readText(dataNode, "releaseNote");
        List<String> urls = new ArrayList<>();
        JsonNode urlsNode = dataNode.path("downloadUrls");
        if (urlsNode.isArray()) {
            for (JsonNode urlNode : urlsNode) {
                String url = urlNode.asText("");
                if (!url.trim().isEmpty()) {
                    urls.add(url.trim());
                }
            }
        }
        info.downloadUrls = urls;
        return info;
    }

    /**
     * 读取文本字段，字段缺失或为null时返回null
     *
     * @param node      JSON对象节点
     * @param fieldName 字段名
     * @return 字段文本值或null
     */
    private static String readText(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        return field.asText();
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public String getAction() {
        return action;
    }

    public String getLatestVersionCode() {
        return latestVersionCode;
    }

    public String getLatestVersionName() {
        return latestVersionName;
    }

    public String getReleaseNote() {
        return releaseNote;
    }

    public List<String> getDownloadUrls() {
        return downloadUrls;
    }

    /**
     * 输出更新信息摘要，便于日志排查
     *
     * @return 摘要字符串
     */
    @Override
    public String toString() {
        return "UpdateInfo{needUpdate=" + needUpdate
                + ", action=" + action
                + ", latestVersionCode=" + latestVersionCode
                + ", latestVersionName=" + latestVersionName
                + ", downloadUrls=" + downloadUrls.size() + "个}";
    }
}
