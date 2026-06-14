package com.tbs.sql.model.runtime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.mapping.SqlCommandType;

import java.io.Serializable;

/**
 * SQL运行时状态，用于监控记录
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlRuntimeStatus implements Serializable {
    private static final long serialVersionUID = -6384000507868094188L;
    /** SQL命令类型 */
    private SqlCommandType commandType;
    /** SQL语句 */
    private String prepareSql;
    /** 执行时间 */
    private long executeTime;
    /** 参数对象 */
    private Object parameterObject;
    /** 结果对象 */
    private Object resultObject;
    /** 异常信息 */
    private Throwable error;
}
