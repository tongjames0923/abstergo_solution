package com.tbs.sql.logger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.mapping.SqlCommandType;

import java.io.Serializable;

/**
 * sql运行时状态,用于监控记录
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlRuntimeStatus implements Serializable {

    private static final long serialVersionUID = -6384000507868094188L;
    /**
     * sql命令类型
     */
    private SqlCommandType commandType;
    /**
     * sql语句
     */
    private String prepareSql;
    /**
     * 执行时间
     */
    private long executeTime;
    /**
     * 参数对象
     */
    private Object parameterObject;
    /**
     * 结果对象
     */
    private Object resultObject;

    /**
     * 异常信息（SQL执行失败时记录）
     */
    private Throwable error;
}
