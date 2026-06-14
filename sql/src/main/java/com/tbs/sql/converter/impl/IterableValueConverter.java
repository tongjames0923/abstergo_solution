package com.tbs.sql.converter.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.sql.converter.IValueConverter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 可迭代值转换器实现类，处理集合和数组类型的值转换。
 *
 * <p>该类实现了 {@link IValueConverter} 接口，专门用于处理 {@link Iterable} 类型的值转换。
 * 它会遍历集合中的每个元素，使用其他转换器进行转换，然后组合成SQL IN语句格式。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>处理集合类型的值转换</li>
 *   <li>遍历集合元素，使用其他转换器进行转换</li>
 *   <li>生成SQL IN语句格式的字符串</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>处理List、Set等集合类型的值转换</li>
 *   <li>生成IN语句的参数值</li>
 *   <li>处理数组类型的值转换</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 输入: [1, 2, 3]
 * // 输出: '1','2','3'
 * 
 * // 输入: ["a", "b", "c"]
 * // 输出: 'a','b','c'
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>需要注入其他 {@link IValueConverter} 实现来处理元素转换</li>
 *   <li>集合为空时会抛出异常</li>
 *   <li>每个元素都会被单引号包围</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see IValueConverter
 * @see CommonValueConverter
 */
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
