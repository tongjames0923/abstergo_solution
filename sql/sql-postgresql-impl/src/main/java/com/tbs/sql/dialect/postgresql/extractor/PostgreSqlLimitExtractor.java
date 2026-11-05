package com.tbs.sql.dialect.postgresql.extractor;

import com.tbs.sql.dialect.ILimitExtractor;
import com.tbs.sql.model.limit.LimitModel;

/**
 * PostgreSQL LIMIT（分页）语句提取器。
 *
 * <p>该类实现了 {@link ILimitExtractor} 接口，提供了PostgreSQL特有的分页语法支持。
 * PostgreSQL使用 {@code LIMIT count OFFSET offset} 语法实现分页。</p>
 *
 * <h3>与MySQL的区别</h3>
 * <ul>
 *   <li>MySQL使用 {@code LIMIT offset, count} 语法（offset在前）</li>
 *   <li>PostgreSQL使用 {@code LIMIT count OFFSET offset} 语法（count在前，OFFSET可省略）</li>
 * </ul>
 *
 * <h3>生成示例</h3>
 * <pre>{@code
 * // offset=0, count=10
 * // 结果: LIMIT 10
 *
 * // offset=20, count=10
 * // 结果: LIMIT 10 OFFSET 20
 *
 * // count=null
 * // 结果: (空字符串，不分页)
 * }</pre>
 *
 * @author abstergo
 * @since 2.0.0
 * @see ILimitExtractor
 * @see LimitModel
 */
public class PostgreSqlLimitExtractor implements ILimitExtractor {

    @Override
    public String extract(LimitModel model) {
        if (model == null) return "";
        Long offset = model.getOffset();
        Long count = model.getCount();
        if (count == null) return "";
        if (offset == null || offset == 0L) return " LIMIT " + count;
        return String.format(" LIMIT %d OFFSET %d", count, offset);
    }
}
