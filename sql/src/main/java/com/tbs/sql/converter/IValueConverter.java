package com.tbs.sql.converter;

/**
 * 值转换器接口，用于将Java对象转换为SQL字符串表示。
 *
 * <p>该接口定义了值转换的标准方法，用于将不同类型的Java对象转换为可以在SQL语句中
 * 使用的字符串表示。支持类型判断、优先级控制等功能。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>判断是否支持特定类型的值转换 {@link #support(Class, Object)}</li>
 *   <li>将值转换为SQL字符串 {@link #convert(Class, Object)}</li>
 *   <li>控制转换器优先级 {@link #getPriority()}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>将Java对象转换为SQL参数值</li>
 *   <li>处理不同类型的值转换逻辑</li>
 *   <li>实现自定义的值转换策略</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class CustomValueConverter implements IValueConverter {
 *     @Override
 *     public boolean support(Class<?> type, Object oldValue) {
 *         return type == CustomType.class;
 *     }
 *     
 *     @Override
 *     public String convert(Class<?> type, Object oldValue) {
 *         return "converted_value";
 *     }
 *     
 *     @Override
 *     public int getPriority() {
 *         return 0;
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>优先级值越小，优先级越高</li>
 *   <li>实现类需要正确处理null值</li>
 *   <li>转换结果需要符合SQL语法要求</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see CommonValueConverter
 * @see IterableValueConverter
 */
public interface IValueConverter {
    /**
     * 判断是否支持指定类型的值转换。
     *
     * @param type 值的类型
     * @param oldValue 原始值
     * @return 如果支持该类型的转换返回true，否则返回false
     */
    boolean support(Class<?> type, Object oldValue);

    /**
     * 将值转换为SQL字符串表示。
     *
     * @param type 值的类型
     * @param oldValue 原始值
     * @return 转换后的SQL字符串
     */
    String convert(Class<?> type, Object oldValue);

    /**
     * 获取转换器的优先级，值越小优先级越高。
     *
     * @return 优先级值
     */
    int getPriority();
}
