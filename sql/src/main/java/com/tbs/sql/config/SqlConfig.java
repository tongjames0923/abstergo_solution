package com.tbs.sql.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.sql.constants.OrderConstant;
import com.tbs.sql.ef.IEntityQueryWrapper;
import com.tbs.sql.ef.impl.SimpleEntityQueryWrapper;
import com.tbs.sql.framework.assembler.ISqlAssembler;
import com.tbs.sql.framework.assembler.impl.DefaultSqlAssembler;
import com.tbs.sql.framework.extractor.ISelectSqlDialect;
import com.tbs.sql.framework.intercepter.SqlAutoFillIntercepter;
import com.tbs.sql.framework.intercepter.SqlLoggingInterceptor;
import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.logger.impl.NoSqlLogger;
import com.tbs.sql.mapper.impl.FetchMapper;
import com.tbs.sql.order.impl.AscStaticOrder;
import com.tbs.sql.order.impl.DescStaticOrder;
import com.tbs.sql.order.impl.OrderedModelOrderImpl;
import com.tbs.sql.properties.SqlProperty;
import com.tbs.sql.utils.QueryUtil;
import com.tbs.sql.utils.TransactionUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.PlatformTransactionManager;
import tk.mybatis.spring.annotation.MapperScan;

import javax.annotation.Resource;

/**
 * SQL模块配置类
 * <p>
 * 提供SQL相关的各种Bean配置，包括SQL组装器、查询工具、事务工具、拦截器等
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@MapperScan(basePackages = "com.tbs.sql.mapper")
public class SqlConfig {

    /**
     * SQL配置属性
     */
    @Resource
    SqlProperty sqlProperty;

    /**
     * 创建FetchMapper实例
     *
     * @return FetchMapper实例
     */
    @Bean
    FetchMapper fetchMapper() {
        return new FetchMapper();
    }

    /**
     * 创建系统SQL组装器
     * <p>
     * 根据配置的sqlAssemblerType创建对应的SQL组装器实例
     * </p>
     *
     * @return SQL组装器实例
     * @throws Throwable 创建过程中可能出现的异常
     */
    @Bean
    public ISqlAssembler systemSqlAssembler() throws Throwable {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new DefaultSqlAssembler();
        }, sqlProperty.getSqlAssemblerType(), BeanUtil::buildByEmptyArgs);
    }

    /**
     * 创建查询工具
     *
     * @param dialect     SQL方言
     * @param sqlAssembler SQL组装器
     * @return 查询工具实例
     */
    @Bean
    public QueryUtil sqlUtil(ISelectSqlDialect dialect, ISqlAssembler sqlAssembler) {
        return new QueryUtil(dialect, sqlProperty, sqlAssembler);
    }

    /**
     * 创建事务工具
     *
     * @param transactionManager 平台事务管理器
     * @return 事务工具实例
     */
    @Bean
    public TransactionUtil transactionUtil(PlatformTransactionManager transactionManager) {
        return new TransactionUtil(transactionManager);
    }

    /**
     * 创建SQL日志拦截器
     * <p>
     * 仅当tbs.sql.enable-log-interceptor属性为true时创建
     * </p>
     *
     * @return SQL日志拦截器实例
     */
    @Bean
    @ConditionalOnProperty(name = "tbs.sql.enable-log-interceptor", havingValue = "true")
    SqlLoggingInterceptor sqlLoggingInterceptor() {
        return new SqlLoggingInterceptor();
    }

    /**
     * 创建SQL自动填充拦截器
     * <p>
     * 仅当tbs.sql.enable-auto-fill-value-interceptor属性为true时创建
     * </p>
     *
     * @return SQL自动填充拦截器实例
     */
    @Bean
    @ConditionalOnProperty(name = "tbs.sql.enable-auto-fill-value-interceptor", havingValue = "true")
    SqlAutoFillIntercepter autoFillIntercepter() {
        return new SqlAutoFillIntercepter();
    }

    /**
     * 创建默认SQL日志记录器
     * <p>
     * 根据配置的sqlLoggerType创建对应的SQL日志记录器实例
     * 仅当不存在ISqlLogger实例且存在SqlLoggingInterceptor实例时创建
     * </p>
     *
     * @return SQL日志记录器实例
     * @throws Throwable 创建过程中可能出现的异常
     */
    @Bean
    @ConditionalOnMissingBean(ISqlLogger.class)
    @ConditionalOnBean(SqlLoggingInterceptor.class)
    public ISqlLogger defaultSqlLogger() throws Throwable {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new NoSqlLogger();
        }, sqlProperty.getSqlLoggerType(), BeanUtil::buildByEmptyArgs);
    }

    /**
     * 创建升序排序实例
     *
     * @return 升序排序实例
     */
    @Bean(OrderConstant.ASC)
    public AscStaticOrder ascOrder() {
        return new AscStaticOrder();
    }

    /**
     * 创建降序排序实例
     *
     * @return 降序排序实例
     */
    @Bean(OrderConstant.DESC)
    public DescStaticOrder descOrder() {
        return new DescStaticOrder();
    }

    /**
     * 创建有序模型排序实例
     *
     * @return 有序模型排序实例
     */
    @Bean(OrderConstant.ORDERED_MODEL)
    public OrderedModelOrderImpl orderedModelOrderImpl() {
        return new OrderedModelOrderImpl();
    }

    /**
     * 创建实体查询包装器
     * <p>
     * 原型作用域，每次请求都会创建新实例
     * </p>
     *
     * @return 实体查询包装器实例
     */
    @Bean
    @Scope("prototype")
    IEntityQueryWrapper entityWrapper() {
        return new SimpleEntityQueryWrapper();
    }
}
