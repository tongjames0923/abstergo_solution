package com.tbs.common.util.time;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 时间工具类
 * <p>
 * 提供时间相关的工具方法，包括日期格式化常量、UTC 时间获取以及基于 Hutool DateUtil 的日期封装。
 * 封装方法对外仅暴露 JDK 类型（{@link Date}、{@link LocalDateTime}、{@link LocalDate}、
 * {@code long}、{@link String}、{@link ChronoUnit}），不泄漏 Hutool 的 DateTime 等特有类型。
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

    /**
     * 获取当前时间
     *
     * @return 当前时间（JDK Date，非 Hutool DateTime）
     */
    public static Date now() {
        return toJdkDate(DateUtil.date());
    }

    /**
     * 获取当前本地日期时间
     *
     * @return 当前本地日期时间
     */
    public static LocalDateTime nowLocalDateTime() {
        return toLocalDateTime(now());
    }

    /**
     * Date 转 LocalDateTime
     *
     * @param date 日期，可为 null
     * @return 本地日期时间，date 为 null 时返回 null
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : DateUtil.toLocalDateTime(date);
    }

    /**
     * LocalDateTime 转 Date
     *
     * @param dateTime 本地日期时间，可为 null
     * @return 日期，dateTime 为 null 时返回 null
     */
    public static Date toDate(LocalDateTime dateTime) {
        return dateTime == null ? null : Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date 转 LocalDate
     *
     * @param date 日期，可为 null
     * @return 本地日期，date 为 null 时返回 null
     */
    public static LocalDate toLocalDate(Date date) {
        LocalDateTime dateTime = toLocalDateTime(date);
        return dateTime == null ? null : dateTime.toLocalDate();
    }

    /**
     * 格式化日期时间（默认格式 yyyy-MM-dd HH:mm:ss）
     *
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String format(Date date) {
        return format(date, DATE_FORMAT_yyyyMMddHHmmss_STR);
    }

    /**
     * 按指定格式格式化日期
     *
     * @param date    日期
     * @param pattern 日期格式，如 yyyyMMdd
     * @return 格式化后的字符串
     */
    public static String format(Date date, String pattern) {
        return date == null ? null : DateUtil.format(date, pattern);
    }

    /**
     * 按指定格式格式化本地日期时间
     *
     * @param dateTime 本地日期时间
     * @param pattern  日期格式，如 yyyy-MM-dd HH:mm:ss
     * @return 格式化后的字符串
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        return dateTime == null ? null : DateUtil.format(dateTime, pattern);
    }

    /**
     * 按指定格式解析日期字符串
     *
     * @param text    日期字符串
     * @param pattern 日期格式，如 yyyyMMdd
     * @return 解析得到的日期
     */
    public static Date parse(String text, String pattern) {
        return toJdkDate(DateUtil.parse(text, pattern));
    }

    /**
     * 按指定格式解析本地日期时间字符串
     *
     * @param text    日期时间字符串
     * @param pattern 日期格式，如 yyyy-MM-dd HH:mm:ss
     * @return 解析得到的本地日期时间
     */
    public static LocalDateTime parseLocalDateTime(String text, String pattern) {
        return DateUtil.parseLocalDateTime(text, pattern);
    }

    /**
     * 秒级偏移
     *
     * @param date   原始时间
     * @param offset 偏移量，正数向后、负数向前
     * @return 偏移后的时间
     */
    public static Date offsetSecond(Date date, int offset) {
        return toJdkDate(DateUtil.offsetSecond(date, offset));
    }

    /**
     * 分钟级偏移
     *
     * @param date   原始时间
     * @param offset 偏移量，正数向后、负数向前
     * @return 偏移后的时间
     */
    public static Date offsetMinute(Date date, int offset) {
        return toJdkDate(DateUtil.offsetMinute(date, offset));
    }

    /**
     * 小时级偏移
     *
     * @param date   原始时间
     * @param offset 偏移量，正数向后、负数向前
     * @return 偏移后的时间
     */
    public static Date offsetHour(Date date, int offset) {
        return toJdkDate(DateUtil.offsetHour(date, offset));
    }

    /**
     * 天级偏移
     *
     * @param date   原始时间
     * @param offset 偏移量，正数向后、负数向前
     * @return 偏移后的时间
     */
    public static Date offsetDay(Date date, int offset) {
        return toJdkDate(DateUtil.offsetDay(date, offset));
    }

    /**
     * 周级偏移
     *
     * @param date   原始时间
     * @param offset 偏移量，正数向后、负数向前
     * @return 偏移后的时间
     */
    public static Date offsetWeek(Date date, int offset) {
        return toJdkDate(DateUtil.offsetWeek(date, offset));
    }

    /**
     * 月级偏移
     *
     * @param date   原始时间
     * @param offset 偏移量，正数向后、负数向前
     * @return 偏移后的时间
     */
    public static Date offsetMonth(Date date, int offset) {
        return toJdkDate(DateUtil.offsetMonth(date, offset));
    }

    /**
     * 年级偏移
     *
     * @param date   原始时间
     * @param offset 偏移量，正数向后、负数向前
     * @return 偏移后的时间
     */
    public static Date offsetYear(Date date, int offset) {
        return toJdkDate(DateUtil.offset(date, DateField.YEAR, offset));
    }

    /**
     * 获取当天起始时刻
     *
     * @param date 日期
     * @return 当天 00:00:00
     */
    public static Date beginOfDay(Date date) {
        return toJdkDate(DateUtil.beginOfDay(date));
    }

    /**
     * 获取当天结束时刻
     *
     * @param date 日期
     * @return 当天 23:59:59.999
     */
    public static Date endOfDay(Date date) {
        return toJdkDate(DateUtil.endOfDay(date));
    }

    /**
     * 获取当月起始时刻
     *
     * @param date 日期
     * @return 当月第一天 00:00:00
     */
    public static Date beginOfMonth(Date date) {
        return toJdkDate(DateUtil.beginOfMonth(date));
    }

    /**
     * 获取当月结束时刻
     *
     * @param date 日期
     * @return 当月最后一天 23:59:59.999
     */
    public static Date endOfMonth(Date date) {
        return toJdkDate(DateUtil.endOfMonth(date));
    }

    /**
     * 计算两个时间之间的差值
     * <p>
     * 毫秒/秒/分/时/天由 Hutool DateUtil.between 计算，其余单位由 JDK ChronoUnit 计算。
     * </p>
     *
     * @param start 起始时间
     * @param end   结束时间
     * @param unit  时间单位（JDK ChronoUnit）
     * @return end 减 start 的差值
     */
    public static long between(Date start, Date end, ChronoUnit unit) {
        DateUnit dateUnit = switch (unit) {
            case MILLIS -> DateUnit.MS;
            case SECONDS -> DateUnit.SECOND;
            case MINUTES -> DateUnit.MINUTE;
            case HOURS -> DateUnit.HOUR;
            case DAYS -> DateUnit.DAY;
            default -> null;
        };
        return dateUnit == null
            ? unit.between(toLocalDateTime(start), toLocalDateTime(end))
            : DateUtil.between(start, end, dateUnit);
    }

    /**
     * 判断两个日期是否为同一天
     *
     * @param date1 日期一
     * @param date2 日期二
     * @return 同一天返回 true
     */
    public static boolean isSameDay(Date date1, Date date2) {
        return DateUtil.isSameDay(date1, date2);
    }

    /**
     * 判断日期是否在指定区间内（含边界）
     *
     * @param date  待判断日期
     * @param begin 区间起始
     * @param end   区间结束
     * @return 在区间内返回 true
     */
    public static boolean isIn(Date date, Date begin, Date end) {
        return DateUtil.isIn(date, begin, end);
    }

    /**
     * 将 Hutool DateTime 转为 JDK Date，避免 Hutool 特有类型外泄
     *
     * @param date 日期
     * @return JDK Date
     */
    private static Date toJdkDate(Date date) {
        return date instanceof cn.hutool.core.date.DateTime
            ? ((cn.hutool.core.date.DateTime) date).toJdkDate()
            : date;
    }

}
