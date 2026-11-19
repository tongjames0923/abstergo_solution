package com.tbs.app.service.getter;

import com.tbs.script.api.expressible.vo.ResultType;
import com.tbs.script.api.expressible.vo.ResultValue;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间强制转换取值器。
 *
 * <p>把源值解析为日期时间并统一输出 yyyy-MM-dd HH:mm:ss，结果类型声明为 datetime，
 * 便于布尔条件按时间先后比较。支持常见日期字符串、ISO 时间戳与 epoch 毫秒。</p>
 *
 * @author abstergo
 */
@Component
public class ToDateTimeGetter extends AbstractCastValueGetter {

    /**
     * 统一输出格式
     */
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取取值器名称。
     *
     * @return 国际化后的取值器名称
     */
    @Override
    public String getGetterName() {
        return text("value.getter.cast.date-time.name");
    }

    /**
     * 获取取值器 ID。
     *
     * @return 取值器 ID
     */
    @Override
    public String getGetterId() {
        return "ToDateTimeGetter";
    }

    /**
     * 获取取值器描述。
     *
     * @return 国际化后的取值器描述
     */
    @Override
    public String getDescription() {
        return text("value.getter.cast.date-time.description");
    }

    /**
     * 声明日期时间结果类型。
     *
     * @return 日期时间结果类型元信息
     */
    @Override
    public ResultType responseResultType() {
        return ResultType.builder().type("datetime").name(text("value.getter.cast.date-time.result.name"))
            .description(text("value.getter.cast.date-time.result.description")).sort(1).build();
    }

    /**
     * 将源值转换为统一格式的日期时间字符串。
     *
     * @param source   源值字符串
     * @param fallback 转换失败时的默认值
     * @return 日期时间结果；解析失败时返回默认值，默认值为空字符串
     */
    @Override
    protected ResultValue cast(String source, String fallback) {
        String value = source.trim();
        try {
            LocalDateTime dateTime;
            if (value.matches("\\d{13}")) {
                dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(value)), ZoneId.systemDefault());
            } else if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
                dateTime = LocalDate.parse(value).atStartOfDay();
            } else if (value.endsWith("Z")) {
                dateTime = LocalDateTime.ofInstant(Instant.parse(value), ZoneId.systemDefault());
            } else {
                dateTime = LocalDateTime.parse(value, OUTPUT_FORMATTER);
            }
            return result(dateTime.format(OUTPUT_FORMATTER));
        } catch (Exception e) {
            return result(fallback == null ? "" : fallback);
        }
    }
}
