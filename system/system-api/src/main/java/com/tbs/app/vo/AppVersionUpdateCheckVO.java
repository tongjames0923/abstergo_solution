package com.tbs.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 应用版本升级检查结果视图
 * <p>
 * 面向客户端升级检查接口的返回视图，告知客户端是否需要升级、
 * 升级动作强度以及最新版本的基本信息与下载入口
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppVersionUpdateCheckVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否需要升级
     */
    private Boolean needUpdate;

    /**
     * 升级动作类型，不需要升级时为空
     *
     * @see com.tbs.shared.enums.AppVersionCompatibilityActionEnum
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
     * 最新版本安装包的下载地址
     */
    private String downloadUrl;
}
