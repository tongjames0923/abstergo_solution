package com.tbs.app.service;

import com.tbs.common.data.ITableDataHandler;
import com.tbs.common.data.structs.TableData;
import com.tbs.common.util.base.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimpleJsonLogDataHandler implements ITableDataHandler {
    /**
     * 获取表格数据处理器ID
     *
     * @return
     */
    @Override
    public String getId() {
        return "SimpleJsonLogDataHandler";
    }

    /**
     *
     * 获取表格数据处理器名称
     *
     * @return
     */
    @Override
    public String getName() {
        return "简单表格处理器，输出JSON日志";
    }

    /**
     * 处理表格数据
     *
     * @param table 表格数据
     */
    @Override
    public void handle(TableData table) {
        log.info("SimpleJsonLogDataHandler: {}", JsonUtils.toJSONString(table));
    }
}
