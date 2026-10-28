package com.tbs.common.data;

import com.tbs.common.data.structs.TableData;
import com.tbs.common.provider.IRouteMeta;
import com.tbs.common.provider.annotation.RoutingService;

/**
 * 表格数据处理器
 */
@RoutingService
public interface ITableDataHandler extends IRouteMeta {

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
