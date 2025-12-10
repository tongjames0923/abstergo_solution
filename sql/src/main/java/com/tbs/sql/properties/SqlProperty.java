package com.tbs.sql.properties;

import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.logger.impl.NoSqlLogger;
import com.tbs.sql.framework.assembler.ISqlAssembler;
import com.tbs.sql.framework.assembler.impl.DefaultSqlAssembler;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * SQL模块配置属性类
 * <p>
 * 通过@ConfigurationProperties注解绑定tbs.sql前缀的配置属性
 * 用于配置SQL模块的各种功能开关和行为
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@ConfigurationProperties("tbs.sql")
public class SqlProperty {
    /**
     * 是否启用SQL日志拦截器
     * <p>
     * 默认为false，表示不启用SQL日志拦截器
     * </p>
     */
    private boolean enableLogInterceptor = false;

    /**
     * 是否启用自动填充值拦截器
     * <p>
     * 默认为false，表示不启用自动填充值拦截器
     * </p>
     */
    private boolean enableAutoFillValueInterceptor = false;

    /**
     * SQL日志记录器的类型
     * <p>
     * 默认为NoSqlLogger.class，表示不进行SQL日志记录
     * </p>
     */
    private Class<? extends ISqlLogger> sqlLoggerType = NoSqlLogger.class;

    /**
     * 是否使用原始值
     * <p>
     * 默认为false，表示使用占位符方式执行SQL
     * true表示直接将参数值拼接到SQL中
     * </p>
     */
    private boolean useRawValue = false;

    /**
     * SQL组装器的类型
     * <p>
     * 默认为DefaultSqlAssembler.class，用于组装SQL语句
     * </p>
     */
    private Class<? extends ISqlAssembler> sqlAssemblerType = DefaultSqlAssembler.class;

}
