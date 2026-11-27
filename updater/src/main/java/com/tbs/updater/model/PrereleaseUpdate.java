package com.tbs.updater.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 非正式分支最新版本信息
 * <p>
 * 由升级检查响应的 prereleaseUpdate 节点解析而来，老服务端不返回该节点时为空。
 * 仅用于提示存在测试版本，不改变更新器的主更新流程。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class PrereleaseUpdate {

    /**
     * 客户端当前版本是否低于该测试版本
     */
    private boolean needUpdate;

    /**
     * 版本类型，2-beta、3-alpha、4-rc
     */
    private Integer versionType;

    /**
     * 内部版本号（服务端可能以字符串下发）
     */
    private String versionCode;

    /**
     * 外部版本名称
     */
    private String versionName;

    /**
     * 更新日志
     */
    private String releaseNote;

    /**
     * 安装包下载地址
     */
    private List<String> downloadUrls = Collections.emptyList();

    /**
     * 从JSON节点解析测试版本信息
     *
     * @param node prereleaseUpdate 节点
     * @return 解析后的实例，节点非法时返回null
     */
    public static PrereleaseUpdate parseFromJson(JsonNode node) {
        if (node == null || node.isNull() || !node.isObject()) {
            return null;
        }
        PrereleaseUpdate update = new PrereleaseUpdate();
        update.needUpdate = node.path("needUpdate").asBoolean(false);
        JsonNode typeNode = node.get("versionType");
        if (typeNode != null && typeNode.isNumber()) {
            update.versionType = typeNode.asInt();
        }
        update.versionCode = textOf(node, "versionCode");
        update.versionName = textOf(node, "versionName");
        update.releaseNote = textOf(node, "releaseNote");
        List<String> urls = new ArrayList<>();
        JsonNode urlsNode = node.path("downloadUrls");
        if (urlsNode.isArray()) {
            for (JsonNode urlNode : urlsNode) {
                String url = urlNode.asText("").trim();
                if (!url.isEmpty()) {
                    urls.add(url);
                }
            }
        }
        update.downloadUrls = urls;
        return update;
    }

    /**
     * 读取文本字段
     *
     * @param node      JSON对象节点
     * @param fieldName 字段名
     * @return 字段文本值或null
     */
    private static String textOf(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        return field.asText();
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public Integer getVersionType() {
        return versionType;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getReleaseNote() {
        return releaseNote;
    }

    public List<String> getDownloadUrls() {
        return downloadUrls;
    }
}
