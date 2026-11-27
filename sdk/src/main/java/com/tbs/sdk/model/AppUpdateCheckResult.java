package com.tbs.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

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
     * 最新版本类型，主数据固定为正式版（1-release），历史数据可能为null
     */
    private Integer versionType;

    /**
     * 非正式分支最新版本，老服务端不返回时为null
     */
    private AppPrereleaseUpdateResult prereleaseUpdate;

    /**
     * 安装包下载地址列表（多镜像容灾）
     */
    private List<String> downloadUrls;

    /**
     * 升级动作可读名称（force/recommend/notify），不需要升级时为null。
     * <p>由 {@link #action} 派生的只读属性，不参与JSON反序列化。</p>
     *
     * @return 动作可读名称
     */
    @JsonIgnore
    public String getActionName() {
        return AppUpdateAction.toName(action);
    }
}
