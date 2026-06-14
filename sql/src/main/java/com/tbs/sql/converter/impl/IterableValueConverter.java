package com.tbs.sql.converter.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.sql.converter.IValueConverter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IterableValueConverter implements IValueConverter {
    private List<IValueConverter> converters;

    public IterableValueConverter(List<IValueConverter> converters) {
        this.converters = converters;
        AssertUtil.isTrue(CollUtil.isEmpty(converters), "IterableValueConverter converters is empty");
    }

    private String convert(Object value) {
        for (IValueConverter converter : converters) {
            if (converter.support(value.getClass(), value)) return converter.convert(value.getClass(), value);
        }
        AssertUtil.throwError(() -> new RuntimeException("IterableValueConverter converter not found"));
        return "";
    }

    @Override
    public boolean support(Class<?> type, Object oldValue) {
        return oldValue != null && oldValue instanceof Iterable;
    }

    @Override
    public String convert(Class<?> type, Object oldValue) {
        final StringBuilder sb = new StringBuilder();
        for (final Object v : (Iterable<?>) oldValue) {
            sb.append("'").append(convert(v)).append("',");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public int getPriority() { return 1; }
}
