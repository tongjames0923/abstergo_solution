package com.tbs.sql.properties;

import com.tbs.sql.assembler.ISqlAssembler;
import com.tbs.sql.assembler.impl.DefaultSqlAssembler;
import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.logger.impl.NoSqlLogger;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SQL模块配置属性类
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@ConfigurationProperties("tbs.sql")
public class SqlProperty {
    private boolean enableLogInterceptor = false;
    private boolean enableAutoFillValueInterceptor = false;
    private Class<? extends ISqlLogger> sqlLoggerType = NoSqlLogger.class;
    private boolean useRawValue = false;
    private Class<? extends ISqlAssembler> sqlAssemblerType = DefaultSqlAssembler.class;
}
