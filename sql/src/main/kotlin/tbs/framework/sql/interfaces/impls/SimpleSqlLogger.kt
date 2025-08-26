package tbs.framework.sql.interfaces.impls

import com.tbs.sql.logger.ISqlLogger
import com.tbs.sql.logger.SqlRuntimeStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * 通过 SimpleSqlLogger 实现 ISqlLogger 接口
 * 适用AutoLogger实现的Sql日志记录器
 */
class SimpleSqlLogger : ISqlLogger {

    val logger: Logger = LoggerFactory.getLogger(SimpleSqlLogger::class.java);

    override fun log(sqlRuntimeStatus: SqlRuntimeStatus) {
        logger.info("Execute SQL: ${sqlRuntimeStatus.prepareSql}\nCost Time: ${sqlRuntimeStatus.executeTime}ms")
    }
}
