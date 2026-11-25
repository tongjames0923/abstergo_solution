package com.tbs.sql.dialect;

import java.util.List;

/**
 * UPDATE语句提取器接口，生成按主键更新的语句。
 *
 * <p>单表 UPDATE 在四种主流数据库上语法一致，默认由 {@code CommonUpdateExtractor} 提供，
 * 各方言按需覆写。列与属性顺序一一对应：列名用于 SET 左侧，属性名用于生成
 * {@code #{entity.属性名}} 预编译占位符。</p>
 *
 * @author abstergo
 * @since 3.0.0
 */
public interface IUpdateExtractor {

    /**
     * 构建按主键更新的语句。
     *
     * @param tableName  表名
     * @param columns    参与 SET 的列名清单
     * @param properties 参与 SET 的属性名清单（时间字段带 jdbcType 后缀），顺序与 columns 一致，须原样写入占位符
     * @param idColumn   主键列名
     * @param idProperty 主键属性名
     * @return UPDATE语句
     */
    String buildUpdate(String tableName, List<String> columns, List<String> properties, String idColumn, String idProperty);
}
