package com.tbs.sql.dialect.oracle;

import com.tbs.sql.dialect.extractor.CommonAssembler;

/**
 * Oracle专用SQL组装器。
 *
 * <p>该类继承自 {@link CommonAssembler}，覆写了默认排序SQL，
 * 以处理Oracle特有的分页语法要求：当使用 {@code OFFSET ... FETCH NEXT} 分页时，
 * 必须同时存在ORDER BY子句，否则Oracle会抛出语法错误。</p>
 *
 * <h3>与SqlServerAssembler的区别</h3>
 * <ul>
 *   <li>SqlServerAssembler使用 {@code ORDER BY (SELECT NULL)} 作为默认排序</li>
 *   <li>OracleAssembler使用 {@code ORDER BY (SELECT NULL FROM DUAL)} 作为默认排序（Oracle需要FROM DUAL伪表）</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see com.tbs.sql.dialect.extractor.CommonAssembler
 * @see com.tbs.sql.dialect.oracle.extractor.OracleLimitExtractor
 */
public class OracleAssembler extends CommonAssembler {

    @Override
    protected String getDefaultOrderSql() {
        return com.tbs.sql.dialect.oracle.extractor.OracleLimitExtractor.DEFAULT_ORDER;
    }
}
