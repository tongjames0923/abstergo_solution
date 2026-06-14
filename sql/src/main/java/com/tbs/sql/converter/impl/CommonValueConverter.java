package com.tbs.sql.converter.impl;

import com.tbs.common.util.time.TimeUtil;
import com.tbs.sql.converter.IValueConverter;
import org.springframework.stereotype.Component;

import java.time.temporal.Temporal;
import java.util.Date;

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
