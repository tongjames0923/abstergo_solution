package com.tbs.sql.dialect.sqlserver.extractor;

import com.tbs.sql.dialect.ILimitExtractor;
import com.tbs.sql.model.limit.LimitModel;

/**
 * SQL Server LIMIT（分页）语句提取器。
 *
 * <p>该类实现了 {@link ILimitExtractor} 接口，提供了SQL Server特有的分页语法支持。
 * SQL Server 2012及以上版本使用 {@code OFFSET ... FETCH NEXT ... ROWS ONLY} 语法实现分页。</p>
 *
 * <h3>与MySQL的区别</h3>
 * <ul>
 *   <li>MySQL使用 {@code LIMIT offset, count} 语法</li>
 *   <li>SQL Server使用 {@code OFFSET offset ROWS FETCH NEXT count ROWS ONLY} 语法</li>
 *   <li>SQL Server要求分页查询必须包含ORDER BY子句，否则会报错</li>
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
 *   <li>调用方（Assembler）需要确保当此方法返回非空值时，ORDER BY子句也存在</li>
 *   <li>如果ORDER BY为空，SQL Server会抛出异常，建议在Assemble层补充 {@code ORDER BY (SELECT NULL)}</li>
 *   <li>需要SQL Server 2012或更高版本</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see ILimitExtractor
 * @see LimitModel
 */
public class SqlServerLimitExtractor implements ILimitExtractor {

    /**
     * 当没有ORDER BY子句时使用的默认排序。
     * SQL Server要求OFFSET/FETCH必须配合ORDER BY使用。
     */
    public static final String DEFAULT_ORDER = " ORDER BY (SELECT NULL)";

    /**
     * 判断此分页模型是否需要额外的默认ORDER BY。
     * 如果返回true，Assembler层需要在ORDER BY为空时补充默认排序。
     *
     * @param model 分页模型
     * @return 如果需要额外的默认ORDER BY返回true
     */
    public boolean needDefaultOrder(LimitModel model) {
        return model != null && model.getCount() != null;
    }

    /**
     * 获取默认的ORDER BY子句。
     *
     * @return 默认ORDER BY子句
     */
    public String getDefaultOrder() {
        return DEFAULT_ORDER;
    }

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
