package com.tbs.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 应用版本兼容策略视图
 * <p>
 * 面向兼容策略管理页展示的源目标应用与最低版本要求信息
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppVersionCompatibilityPolicyVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 策略ID
     */
    private Long id;

    /**
     * 源应用标识
     */
    private String sourceAppKey;

    /**
     * 源应用内部版本号，为空表示该源应用所有版本均适用
     */
    private Long sourceVersionCode;

    /**
     * 源平台
     */
    private String sourcePlatform;

    /**
     * 目标应用标识
     */
    private String targetAppKey;

    /**
     * 目标平台
     */
    private String targetPlatform;

    /**
     * 目标应用必须满足的最低内部版本号
     */
    private Long minCompatVersion;

    /**
     * 动作类型
     *
     * @see com.tbs.shared.enums.AppVersionCompatibilityActionEnum
     */
    private Integer action;

    /**
     * 启用状态
     *
     * @see com.tbs.shared.enums.CommonFlagEnum
     */
    private Integer flag;

    /**
     * 创建时间
     */
    private Date createTime;
}
