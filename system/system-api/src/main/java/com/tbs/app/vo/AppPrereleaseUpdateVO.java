package com.tbs.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 非正式分支最新版本更新信息
 * <p>
 * 作为升级检查结果的独立分支返回，与正式版主数据并列。
 * 仅用于提示客户端存在测试版本，不改变主流程的升级判断。
 * </p>
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppPrereleaseUpdateVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 客户端当前版本是否低于该测试版本
     */
    private Boolean needUpdate;

    /**
     * 版本类型，2-beta、3-alpha、4-rc
     *
     * @see com.tbs.shared.enums.AppVersionTypeEnum
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
