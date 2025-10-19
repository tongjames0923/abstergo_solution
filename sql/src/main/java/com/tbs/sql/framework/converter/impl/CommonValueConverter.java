package com.tbs.sql.framework.converter.impl;

import com.tbs.common.util.time.TimeUtil;
import com.tbs.sql.framework.converter.IValueConverter;
import org.springframework.stereotype.Component;

import java.time.temporal.Temporal;
import java.util.Date;

/**
 * 常用类型值转换器，包含字符串、数字、布尔、时间、日期（CharSequence,Number,Boolean,Date,Temporal）
 *
 * @author abstergo
 */
@Component
public class CommonValueConverter implements IValueConverter {
    @Override
    public boolean support(Class<?> type, Object oldValue) {
        if (oldValue == null) {
            return true;
        }
        return oldValue instanceof CharSequence ||
            oldValue instanceof Number ||
            oldValue instanceof Boolean ||
            oldValue instanceof Date ||
            oldValue instanceof Temporal;
    }

    @Override
    public String convert(Class<?> type, Object oldValue) {
        if (oldValue == null) {
            return "null";
        }
        if (oldValue instanceof CharSequence) {
            return oldValue.toString();
        } else if (oldValue instanceof Number) {
            return oldValue.toString();
        } else if (oldValue instanceof Boolean) {
            return oldValue.toString();
        } else if (oldValue instanceof Temporal) {
            final Temporal instant = (Temporal)oldValue;
            return TimeUtil.DATE_FORMAT_TEMPORAL_yyyyMMddHHmmss.format(instant);
        }
        if (oldValue instanceof Date) {
            final Date date = (Date)oldValue;
            return TimeUtil.DATE_FORMAT_yyyyMMddHHmmss.format(date);
        }
        return "";
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
