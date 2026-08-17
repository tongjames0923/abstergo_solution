package com.tbs.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 应用版本列表视图
 * <p>
 * 面向版本列表页展示的裁剪视图，不含更新日志、构建信息等详情字段
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppVersionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 版本ID
     */
    private Long id;

    /**
     * 应用标识
     */
    private String appKey;

    /**
     * 内部版本号
     */
    private Long versionCode;

    /**
     * 外部版本名称
     */
    private String versionName;

    /**
     * 版本类型
     *
     * @see com.tbs.shared.enums.AppVersionTypeEnum
     */
    private Integer versionType;

    /**
     * 平台，如 ANDROID、IOS、WEB
     */
    private String platform;

    /**
     * 生命周期状态
     *
     * @see com.tbs.shared.enums.AppVersionFlagEnum
     */
    private Integer flag;

    /**
     * 发布时间
     */
    private Date releaseTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
