package com.tbs.app.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 下载码统计视图对象
 * <p>
 * 面向管理界面的统计口径：总码数、有效（含永久未过期）、已过期、永久有效
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class OssDownloadCodeStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 下载码总数
     */
    private long total;

    /**
     * 有效数量（永久有效或尚未到期）
     */
    private long valid;

    /**
     * 已过期数量
     */
    private long expired;

    /**
     * 永久有效数量
     */
    private long permanent;
}
