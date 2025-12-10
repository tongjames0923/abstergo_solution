package com.tbs.common.util.time;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 时间工具类
 * <p>
 * 提供时间相关的工具方法，包括日期格式化常量和UTC时间获取功能
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum TimeUtil {
    ;
    /**
     * 日期时间格式字符串（年-月-日 时:分:秒）
     */
    public static final String DATE_FORMAT_yyyyMMddHHmmss_STR = "yyyy-MM-dd HH:mm:ss";
    /**
     * 日期时间格式字符串（年-月-日 时:分）
     */
    public static final String DATE_FORMAT_yyyyMMddHHmm_STR = "yyyy-MM-dd HH:mm";
    /**
     * 日期时间格式化对象（年-月-日 时:分:秒）
     */
    public static final SimpleDateFormat DATE_FORMAT_yyyyMMddHHmmss =
        new SimpleDateFormat(TimeUtil.DATE_FORMAT_yyyyMMddHHmmss_STR);
    /**
     * 日期时间格式化对象（年-月-日 时:分）
     */
    public static final SimpleDateFormat DATE_FORMAT_yyyyMMddHHmm =
        new SimpleDateFormat(TimeUtil.DATE_FORMAT_yyyyMMddHHmm_STR);
    /**
     * JDK 8+日期时间格式化器（年-月-日 时:分:秒）
     */
    public static final DateTimeFormatter DATE_FORMAT_TEMPORAL_yyyyMMddHHmmss =
        DateTimeFormatter.ofPattern(TimeUtil.DATE_FORMAT_yyyyMMddHHmmss_STR);

    /**
     * JDK 8+日期时间格式化器（年-月-日 时:分）
     */
    public static final DateTimeFormatter DATE_FORMAT_TEMPORAL_yyyyMMddHHmm =
        DateTimeFormatter.ofPattern(TimeUtil.DATE_FORMAT_yyyyMMddHHmm_STR);

    /**
     * 获取UTC时间戳
     * <p>
     * 根据指定的时间单位返回当前UTC时间戳
     * </p>
     *
     * @param timeUnit 时间单位
     * @return UTC时间戳，单位由参数指定
     */
    public static long getUtcTime(TimeUnit timeUnit) {
        return timeUnit.convert(getUtcTime(), TimeUnit.MILLISECONDS);
    }

    /**
     * 获取UTC时间戳（毫秒单位）
     * <p>
     * 返回当前UTC时间的毫秒级时间戳
     * </p>
     *
     * @return UTC时间戳（毫秒单位）
     */
    public static long getUtcTime() {
        return LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

}
