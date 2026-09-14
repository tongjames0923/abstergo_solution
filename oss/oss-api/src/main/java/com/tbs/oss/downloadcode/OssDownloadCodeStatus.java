package com.tbs.oss.downloadcode;

/**
 * 下载码状态枚举
 * <p>
 * 由过期时间戳推导的查询态：永久有效（expireAt小于等于0）、已过期（已超过expireAt）、
 * 有效（永久或尚未到期）；用于列表筛选与统计口径统一
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum OssDownloadCodeStatus {

    /**
     * 有效：永久有效或尚未到期
     */
    VALID,

    /**
     * 已过期：过期时间戳大于0且已超过当前时间
     */
    EXPIRED,

    /**
     * 永久有效：过期时间戳小于等于0
     */
    PERMANENT
}
