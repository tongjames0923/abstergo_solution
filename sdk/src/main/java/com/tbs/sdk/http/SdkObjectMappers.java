package com.tbs.sdk.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * SDK统一ObjectMapper工厂
 * <p>
 * 后端全局配置 spring.jackson.date-format 为 {@code yyyy-MM-dd HH:mm:ss}、time-zone 为 GMT+8，
 * 所有 {@link java.util.Date} 字段（如 TokenModel.expireTime、PushNotifyMessage.createTime）均按此
 * 格式序列化。Jackson默认的 StdDateFormat 仅识别 ISO-8601 与时间戳，直接反序列化会抛
 * InvalidFormatException。此处统一对齐后端日期格式与时区，并忽略未知属性，供HTTP与SSE两个
 * JSON入口共用，避免各处重复配置导致漂移。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class SdkObjectMappers {

    /**
     * 后端全局日期序列化格式（对齐 spring.jackson.date-format）
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 后端全局日期时区（对齐 spring.jackson.time-zone）
     */
    private static final String TIME_ZONE = "GMT+8";

    /**
     * 工具类禁止实例化
     */
    private SdkObjectMappers() {
    }

    /**
     * 构建与后端日期格式/时区对齐、忽略未知属性的ObjectMapper
     *
     * @return ObjectMapper
     */
    public static ObjectMapper newMapper() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        return new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDateFormat(dateFormat);
    }
}
