package com.tbs.sql.converter.impl;

import com.tbs.common.util.time.TimeUtil;
import com.tbs.sql.converter.IValueConverter;
import org.springframework.stereotype.Component;

import java.time.temporal.Temporal;
import java.util.Date;

/**
 * 通用值转换器实现类，处理常见Java类型的值转换。
 *
 * <p>该类实现了 {@link IValueConverter} 接口，提供了对常见Java类型的值转换支持，
 * 包括字符串、数字、布尔值、日期、时间等类型的转换。</p>
 *
 * <h3>支持的类型</h3>
 * <ul>
 *   <li>{@link CharSequence}: 字符串类型，直接返回toString()</li>
 *   <li>{@link Number}: 数字类型，直接返回toString()</li>
 *   <li>{@link Boolean}: 布尔类型，直接返回toString()</li>
 *   <li>{@link Date}: 日期类型，格式化为yyyy-MM-dd HH:mm:ss</li>
 *   <li>{@link java.time.temporal.Temporal}: 时间类型，格式化为yyyy-MM-dd HH:mm:ss</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>处理基本类型的值转换</li>
 *   <li>处理日期时间类型的格式化</li>
 *   <li>作为默认的值转换器使用</li>
 * </ul>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>null值会返回字符串"null"</li>
 *   <li>不支持的类型会返回空字符串</li>
 *   <li>日期格式使用标准的yyyy-MM-dd HH:mm:ss格式</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see IValueConverter
 * @see IterableValueConverter
 */
@Component
public class CommonValueConverter implements IValueConverter {
    @Override
    public boolean support(Class<?> type, Object oldValue) {
        if (oldValue == null) return true;
        return oldValue instanceof CharSequence || oldValue instanceof Number || oldValue instanceof Boolean || oldValue instanceof Date || oldValue instanceof Temporal;
    }

    @Override
    public String convert(Class<?> type, Object oldValue) {
        if (oldValue == null) return "null";
        if (oldValue instanceof CharSequence) return oldValue.toString();
        if (oldValue instanceof Number) return oldValue.toString();
        if (oldValue instanceof Boolean) return oldValue.toString();
        if (oldValue instanceof Temporal) return TimeUtil.DATE_FORMAT_TEMPORAL_yyyyMMddHHmmss.format((Temporal) oldValue);
        if (oldValue instanceof Date) return TimeUtil.DATE_FORMAT_yyyyMMddHHmmss.format((Date) oldValue);
        return "";
    }

    @Override
    public int getPriority() { return 0; }
}
