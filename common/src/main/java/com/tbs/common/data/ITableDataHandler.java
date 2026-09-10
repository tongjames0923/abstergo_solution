package com.tbs.common.data;

import com.tbs.common.data.structs.TableData;

/**
 * 表格数据处理器
 */
public interface ITableDataHandler {

    /**
     * 获取表格数据处理器ID
     *
     * @return
     */
    String getId();

    /**
     *
     * 获取表格数据处理器名称
     *
     * @return
     */
    String getName();

    /**
     * 处理表格数据
     *
     * @param table 表格数据
     */
    void handle(TableData table);

}
