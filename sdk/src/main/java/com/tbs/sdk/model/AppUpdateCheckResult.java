package com.tbs.sdk.model;

import java.util.List;
import lombok.Data;

/**
 * 应用升级检查结果
 * <p>
 * 对应服务端AppVersionUpdateCheckVO：needUpdate为false时其余字段为空，
 * downloadUrls为最新版本安装包下载地址（OSS预签名URL或外部托管链接）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class AppUpdateCheckResult {

    /**
     * 是否需要升级
     */
    private Boolean needUpdate;

    /**
     * 升级动作类型（不需要升级时为null，语义见服务端AppVersionCompatibilityActionEnum）
     */
    private Integer action;

    /**
     * 最新已发布版本的内部版本号
     */
    private Long latestVersionCode;

    /**
     * 最新已发布版本的外部版本名称
     */
    private String latestVersionName;

    /**
     * 最新版本的更新日志
     */
    private String releaseNote;

    /**
     * 安装包下载地址列表（多镜像容灾）
     */
    private List<String> downloadUrls;
}
