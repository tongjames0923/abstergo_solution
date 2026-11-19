package com.tbs.app.service.getter;

import com.tbs.script.api.expressible.vo.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

/**
 * 日期时间取值器。
 *
 * <p>按类型输出当前日期时间的各个维度，作为时间窗口类事件条件来源。支持可选的 Java ZoneId 时区参数，
 * 时区非法时回退系统默认；类型未知或取值异常时返回空字符串，不中断脚本执行。</p>
 *
 * @author abstergo
 */
@Component
public class DateTimeGetter extends AbstractStringValueGetter {

    /**
     * 时间类型枚举值：毫秒时间戳
     */
    private static final String TYPE_TIMESTAMP = "timestamp";

    /**
     * 时间类型枚举值：完整日期时间
     */
    private static final String TYPE_DATETIME = "datetime";

    /**
     * 时间类型枚举值：日期
     */
    private static final String TYPE_DATE = "date";

    /**
     * 时间类型枚举值：时间
     */
    private static final String TYPE_TIME = "time";

    /**
     * 时间类型枚举值：年
     */
    private static final String TYPE_YEAR = "year";

    /**
     * 时间类型枚举值：月
     */
    private static final String TYPE_MONTH = "month";

    /**
     * 时间类型枚举值：日
     */
    private static final String TYPE_DAY = "day";

    /**
     * 时间类型枚举值：星期
     */
    private static final String TYPE_DAY_OF_WEEK = "dayOfWeek";

    /**
     * 时间类型枚举值：小时
     */
    private static final String TYPE_HOUR = "hour";

    /**
     * 完整日期时间格式
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 日期格式
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 时间格式
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * 时间类型文案 key 前缀
     */
    private static final String I18N_PREFIX = "value.getter.date-time.type.";

    /**
     * 常用时区候选值，仅作为输入建议，用户可填写任意合法 ZoneId
     */
    private static final List<String> COMMON_ZONES = List.of("Asia/Shanghai", "Asia/Tokyo", "Asia/Singapore", "UTC",
        "Europe/London", "Europe/Berlin", "America/New_York", "America/Los_Angeles", "Australia/Sydney");

    /**
     * 获取取值器名称。
     *
     * @return 国际化后的取值器名称
     */
    @Override
    public String getGetterName() {
        return text("value.getter.date-time.name");
    }

    /**
     * 获取取值器 ID。
     *
     * @return 取值器 ID
     */
    @Override
    public String getGetterId() {
        return "DateTimeGetter";
    }

    /**
     * 获取取值器描述。
     *
     * @return 国际化后的取值器描述
     */
    @Override
    public String getDescription() {
        return text("value.getter.date-time.description");
    }

    /**
     * 声明参数定义：时间类型枚举与可选时区。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        List<EnumValue> enumValues = List.of(enumValue(TYPE_TIMESTAMP, "timestamp"), enumValue(TYPE_DATETIME,
            "datetime"), enumValue(TYPE_DATE, "date"), enumValue(TYPE_TIME, "time"), enumValue(TYPE_YEAR, "year"),
            enumValue(TYPE_MONTH, "month"), enumValue(TYPE_DAY, "day"), enumValue(TYPE_DAY_OF_WEEK, "day-of-week"),
            enumValue(TYPE_HOUR, "hour"));
        return List.of(ParamType.builder().isEnum(true).type("string")
            .name(text("value.getter.date-time.param.type.name"))
            .description(text("value.getter.date-time.param.type.description")).enumValues(enumValues)
            .required(true).sort(1)
            .ui(ParamUiMeta.builder().inputType("select")
                .placeholder(text("value.getter.date-time.param.type.placeholder")).build())
            .build(),
            ParamType.builder().type("string").name(text("value.getter.date-time.param.zone.name"))
                .description(text("value.getter.date-time.param.zone.description")).enumValues(zoneCandidates())
                .sort(2)
                .ui(ParamUiMeta.builder().inputType("select").filterable(true).allowCreate(true)
                    .placeholder(text("value.getter.date-time.param.zone.placeholder")).build())
                .build());
    }

    /**
     * 构造常用时区候选值。
     *
     * @return 时区候选值列表
     */
    private List<EnumValue> zoneCandidates() {
        return COMMON_ZONES.stream().map(zone -> EnumValue.builder().value(zone).label(zone).build()).toList();
    }

    /**
     * 按类型读取当前日期时间。
     *
     * @param paramValues 参数值集合，首个参数为时间类型，第二个参数为可选时区
     * @return 字符串结果；类型未知或异常时返回空字符串
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        String type = paramString(paramValues, 0);
        if (type == null || type.isBlank()) {
            return emptyResult();
        }
        try {
            ZoneId zone = resolveZone(paramString(paramValues, 1));
            LocalDateTime now = LocalDateTime.now(zone);
            return stringResult(switch (type) {
                case TYPE_TIMESTAMP -> String.valueOf(now.atZone(zone).toInstant().toEpochMilli());
                case TYPE_DATETIME -> now.format(DATETIME_FORMATTER);
                case TYPE_DATE -> now.format(DATE_FORMATTER);
                case TYPE_TIME -> now.format(TIME_FORMATTER);
                case TYPE_YEAR -> String.valueOf(now.getYear());
                case TYPE_MONTH -> String.valueOf(now.getMonthValue());
                case TYPE_DAY -> String.valueOf(now.getDayOfMonth());
                case TYPE_DAY_OF_WEEK -> String.valueOf(now.getDayOfWeek().getValue());
                case TYPE_HOUR -> String.valueOf(now.getHour());
                default -> "";
            });
        } catch (Exception e) {
            return emptyResult();
        }
    }

    /**
     * 解析时区参数。
     *
     * @param zoneId 时区 ID，可为空
     * @return 解析后的时区；参数为空或非法时使用系统默认时区
     */
    private ZoneId resolveZone(String zoneId) {
        if (zoneId == null || zoneId.isBlank()) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(zoneId);
        } catch (Exception e) {
            return ZoneId.systemDefault();
        }
    }

    /**
     * 构造时间类型枚举项。
     *
     * @param value    枚举值
     * @param labelKey 文案 key 后缀
     * @return 枚举项
     */
    private EnumValue enumValue(String value, String labelKey) {
        return EnumValue.builder().value(value).label(text(I18N_PREFIX + labelKey)).build();
    }
}
