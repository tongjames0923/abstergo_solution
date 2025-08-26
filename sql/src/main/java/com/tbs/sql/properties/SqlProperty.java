package com.tbs.sql.properties;

import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.logger.impl.NoSqlLogger;
import com.tbs.sql.framework.ISqlAssembler;
import com.tbs.sql.query.impl.DefaultSqlAssembler;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * @author abstergo
 */
@Data
@ConfigurationProperties("tbs.sql")
public class SqlProperty {
    /**
     * 是否启用SQL日志拦截器的标志，默认不启用
     */
    private boolean enableLogInterceptor = false;

    /**
     * 是否启用自动填充值拦截器的标志，默认不启用
     */
    private boolean enableAutoFillValueInterceptor = false;

    /**
     * SQL日志记录器的类型，默认为NoSqlLogger，表示不进行SQL日志记录
     */
    private Class<? extends ISqlLogger> sqlLoggerType = NoSqlLogger.class;

    /**
     * 是否使用原始值，默认为false，表示使用占位符
     */
    private boolean useRawValue = false;

    /**
     * SQL组装器的类型，默认为DefaultSqlAssembler
     */
    private Class<? extends ISqlAssembler> sqlAssemblerType = DefaultSqlAssembler.class;

}
