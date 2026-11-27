package com.tbs.sdk.model;

import lombok.Data;

import java.util.List;

/**
 * 非正式分支最新版本更新信息
 * <p>
 * 对应服务端 AppPrereleaseUpdateVO，老服务端不返回该字段时为null。
 * 仅用于提示存在测试版本，不参与 needUpdate / action 判定。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class AppPrereleaseUpdateResult {

    /**
     * 客户端当前版本是否低于该测试版本
     */
    private Boolean needUpdate;

    /**
     * 版本类型，2-beta、3-alpha、4-rc
     */
    private Integer versionType;

    /**
     * 内部版本号
     */
    private Long versionCode;

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
    private List<String> downloadUrls;
}
