package tbs.framework.sql.interfaces.impls;

import com.tbs.sql.logger.ISqlLogger;
import com.tbs.sql.logger.SqlRuntimeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通过 SimpleSqlLogger 实现 ISqlLogger 接口
 * 适用AutoLogger实现的Sql日志记录器
 */
public class SimpleSqlLogger implements ISqlLogger {

    private static final Logger logger = LoggerFactory.getLogger(SimpleSqlLogger.class);

    @Override
    public void log(SqlRuntimeStatus sqlRuntimeStatus) {
        logger.info("Execute SQL: " + sqlRuntimeStatus.getPrepareSql() + "\nCost Time: " + sqlRuntimeStatus.getExecuteTime() + "ms");
    }
}