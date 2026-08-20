package com.tbs.updater.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 待下载的更新文件
 * <p>
 * 每个文件包含期望的本地文件名与一个或多个下载地址。
 * 多地址场景下按顺序尝试，配合 tryAllUrls 配置实现镜像容灾。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DownloadFile {

    /**
     * 期望的本地文件名（如 app.jar、config.zip）
     */
    private String name;

    /**
     * 下载地址列表（至少一个）
     */
    private List<String> downloadUrls = Collections.emptyList();

    /**
     * 从JSON节点解析单个文件信息
     * <p>
     * 支持两种格式：
     * 1. 对象格式：{"name": "app.jar", "downloadUrls": [...]}
     * 2. 纯字符串：当作URL处理，文件名从URL自动提取
     * </p>
     *
     * @param node JSON节点
     * @return 解析后的文件对象，节点非法时返回null
     */
    public static DownloadFile parseFromJson(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        DownloadFile file = new DownloadFile();
        if (node.isObject()) {
            JsonNode nameNode = node.get("name");
            if (nameNode != null && !nameNode.isNull()) {
                file.name = nameNode.asText().trim();
            }
            List<String> urls = new ArrayList<>();
            JsonNode urlsNode = node.path("downloadUrls");
            if (urlsNode.isArray()) {
                for (JsonNode urlNode : urlsNode) {
                    String url = urlNode.asText("");
                    if (!url.trim().isEmpty()) {
                        urls.add(url.trim());
                    }
                }
            } else {
                /*
                 * 兼容单个 downloadUrl 字符串字段
                 */
                JsonNode singleNode = node.get("downloadUrl");
                if (singleNode != null && !singleNode.isNull()) {
                    String url = singleNode.asText("").trim();
                    if (!url.isEmpty()) {
                        urls.add(url);
                    }
                }
            }
            file.downloadUrls = urls;
        } else if (node.isTextual()) {
            /*
             * 纯字符串视为单个URL，文件名由Downloader从URL提取
             */
            String url = node.asText("").trim();
            if (!url.isEmpty()) {
                file.downloadUrls = Collections.singletonList(url);
            }
        }
        return file;
    }

    public String getName() {
        return name;
    }

    public List<String> getDownloadUrls() {
        return downloadUrls;
    }

    /**
     * 从URL列表创建文件对象（无指定名称）
     *
     * @param urls 下载地址列表
     * @return 文件对象
     */
    public static DownloadFile fromUrls(List<String> urls) {
        DownloadFile file = new DownloadFile();
        file.downloadUrls = urls != null ? urls : Collections.emptyList();
        return file;
    }

    @Override
    public String toString() {
        return "DownloadFile{name='" + name + "', urls=" + downloadUrls.size() + "个}";
    }
}
