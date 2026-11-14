package com.tbs.sql.dialect.oracle.extractor;

import com.tbs.sql.dialect.ILimitExtractor;
import com.tbs.sql.model.limit.LimitModel;

/**
 * Oracle LIMIT（分页）语句提取器。
 *
 * <p>该类实现了 {@link ILimitExtractor} 接口，提供了Oracle特有的分页语法支持。
 * Oracle 12c及以上版本使用 {@code OFFSET offset ROWS FETCH NEXT count ROWS ONLY} 语法实现分页。</p>
 *
 * <h3>与MySQL/SQL Server的区别</h3>
 * <ul>
 *   <li>MySQL使用 {@code LIMIT offset, count} 语法</li>
 *   <li>SQL Server使用 {@code OFFSET offset ROWS FETCH NEXT count ROWS ONLY} 语法</li>
 *   <li>Oracle使用 {@code OFFSET offset ROWS FETCH NEXT count ROWS ONLY} 语法（与SQL Server相同）</li>
 *   <li>Oracle要求OFFSET值必须显式指定（即使为0），与SQL Server一致</li>
 * </ul>
 *
 * <h3>生成示例</h3>
 * <pre>{@code
 * // offset=0, count=10
 * // 结果: OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY
 *
 * // offset=20, count=10
 * // 结果: OFFSET 20 ROWS FETCH NEXT 10 ROWS ONLY
 *
 * // count=null
 * // 结果: (空字符串，不分页)
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>需要Oracle 12c或更高版本</li>
 *   <li>调用方（Assembler）需要确保当此方法返回非空值时，ORDER BY子句也存在</li>
 *   <li>Oracle的FETCH FIRST必须配合ORDER BY使用，否则会报错</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see ILimitExtractor
 * @see LimitModel
 */
public class OracleLimitExtractor implements ILimitExtractor {

    /**
     * 当没有ORDER BY子句时使用的默认排序。
     * Oracle要求OFFSET/FETCH必须配合ORDER BY使用。
     */
    public static final String DEFAULT_ORDER = " ORDER BY (SELECT NULL FROM DUAL)";

    @Override
    public String extract(LimitModel model) {
        if (model == null) return "";
        Long offset = model.getOffset();
        Long count = model.getCount();
        if (count == null) return "";
        if (offset == null) offset = 0L;
        return " OFFSET " + offset + " ROWS FETCH NEXT " + count + " ROWS ONLY";
    }
}
