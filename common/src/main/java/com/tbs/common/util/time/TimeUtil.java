package com.tbs.common.util.time;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * The enum Time util.
 *
 * @author abstergo
 */
public enum TimeUtil {
    ;
    /**
     * The constant DATE_FORMAT_yyyyMMddHHmmss_STR.
     */
    public static final String DATE_FORMAT_yyyyMMddHHmmss_STR = "yyyy-MM-dd HH:mm:ss";
    /**
     * The constant DATE_FORMAT_yyyyMMddHHmm_STR.
     */
    public static final String DATE_FORMAT_yyyyMMddHHmm_STR = "yyyy-MM-dd HH:mm";
    /**
     * The constant DATE_FORMAT_yyyyMMddHHmmss.
     */
    public static final SimpleDateFormat DATE_FORMAT_yyyyMMddHHmmss =
        new SimpleDateFormat(TimeUtil.DATE_FORMAT_yyyyMMddHHmmss_STR);
    /**
     * The constant DATE_FORMAT_yyyyMMddHHmm.
     */
    public static final SimpleDateFormat DATE_FORMAT_yyyyMMddHHmm =
        new SimpleDateFormat(TimeUtil.DATE_FORMAT_yyyyMMddHHmm_STR);
    /**
     * The constant DATE_FORMAT_TEMPORAL_yyyyMMddHHmmss.
     */
    public static final DateTimeFormatter DATE_FORMAT_TEMPORAL_yyyyMMddHHmmss =
        DateTimeFormatter.ofPattern(TimeUtil.DATE_FORMAT_yyyyMMddHHmmss_STR);

    /**
     * The constant DATE_FORMAT_TEMPORAL_yyyyMMddHHmm.
     */
    public static final DateTimeFormatter DATE_FORMAT_TEMPORAL_yyyyMMddHHmm =
        DateTimeFormatter.ofPattern(TimeUtil.DATE_FORMAT_yyyyMMddHHmm_STR);

    /**
     * 获取utc时间戳，
     *
     * @param timeUnit 时间单位
     * @return
     */
    public static long getUtcTime(TimeUnit timeUnit) {
        return timeUnit.convert(getUtcTime(), TimeUnit.MILLISECONDS);
    }

    /**
     * 获取utc时间戳，单位毫秒
     *
     * @return
     */
    public static long getUtcTime() {
        return LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

}
