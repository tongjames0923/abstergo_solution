package com.tbs.sql.dialect;

import com.tbs.sql.dialect.extractor.CommonInsertExtractor;

import java.util.List;

/**
 * INSERT语句提取器接口，用于生成跨数据库兼容的插入语句。
 *
 * <p>该接口负责批量插入的语法差异与容量上限，是四库兼容的关键扩展点。
 * 主流数据库中只有 Oracle 的多行插入语法与其余三家不同，因此实现分两类：
 * 标准多行 {@code VALUES} 与 Oracle 的 {@code INSERT ALL}。</p>
 *
 * <h3>能力边界</h3>
 * <ul>
 *   <li>只负责 INSERT 语句文本生成，不负责参数绑定与执行</li>
 *   <li>行数与参数上限由方言实现声明，分批逻辑由调用方承担</li>
 *   <li>单行插入在四种数据库上语法一致，无需经过本接口</li>
 * </ul>
 *
 * <h3>各库差异</h3>
 * <ul>
 *   <li>MySQL / PostgreSQL：标准多行 {@code VALUES (...), (...)}，无硬性行数上限</li>
 *   <li>SQL Server：支持标准多行，但单条语句最多 1000 行、2100 个绑定参数</li>
 *   <li>Oracle：23ai 之前不支持多行 {@code VALUES}，必须使用 {@code INSERT ALL}</li>
 * </ul>
 *
 * @author abstergo
 * @since 3.0.0
 * @see CommonInsertExtractor
 * @see com.tbs.sql.dialect.oracle.extractor.OracleInsertExtractor
 */
public interface IInsertExtractor {
    /**
     * 构建批量插入语句。
     *
     * <p>参数占位符按 {@code #{records[行下标].属性名}} 形式生成，属性名取自属性清单，
     * 与调用方传入的参数表结构一一对应。参数名 {@code records} 为批量插入方法的固定约定名。</p>
     *
     * @param tableName  表名
     * @param columns    列名清单，用于拼接插入列清单
     * @param properties 属性名清单，用于生成参数占位符，顺序与列名清单一致
     * @param rowCount   行数，必须大于0
     * @return 批量插入SQL
     */
    String buildBatchInsert(String tableName, List<String> columns, List<String> properties, int rowCount);

    /**
     * 获取单条语句允许的最大行数。
     *
     * @return 最大行数，默认5000
     */
    default int maxRows() {
        return 5000;
    }

    /**
     * 获取单条语句允许的最大绑定参数数。
     *
     * @return 最大参数数，默认3000
     */
    default int maxParameters() {
        return 3000;
    }
}
