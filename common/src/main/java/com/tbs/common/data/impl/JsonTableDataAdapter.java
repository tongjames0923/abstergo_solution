package com.tbs.common.data.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.common.data.ITableDataAdapter;
import com.tbs.common.data.structs.TableData;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON 格式表格数据适配器
 * <p>
 * 将 TableData 与 JSON 格式互转。JSON 结构如下：
 * <pre>{@code
 * {
 *   "tableId": "order-001",
 *   "tableName": "订单明细",
 *   "columns": [{"name": "商品名称", "index": 0}],
 *   "rows": [{"cells": ["手机", "2"], "index": 0}]
 * }
 * }</pre>
 * <p>
 * 读取时通过 {@link TableData.Builder} 构建不可变 TableData；
 * 写入时利用 Jackson 直接序列化（TableData 的 @Getter 提供 getter）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class JsonTableDataAdapter implements ITableDataAdapter {

    /**
     * 从输入流读取 JSON 并解析为 TableData
     *
     * @param stream 输入流，不能为 null
     * @return 不可变 TableData 实例
     */
    @Override
    public TableData adapt(InputStream stream) {
        AssertUtil.isTrue(stream == null)
            .i18n("table.adapter.stream.null")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        try {
            return doAdapt(stream);
        } catch (IOException e) {
            throw AssertUtil.isTrue(true)
                .i18n("table.adapter.json.parse.error", e.getMessage())
                .errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
                .cause(e)
                .build();
        }
    }

    /**
     * 将 TableData 序列化为 JSON 字节数组
     *
     * @param tableData 表格数据，不能为 null
     * @return UTF-8 编码的 JSON 字节数组
     */
    @Override
    public byte[] write(TableData tableData) {
        AssertUtil.isTrue(tableData == null)
            .i18n("table.adapter.write.data.null")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        try {
            return JsonUtils.getMapperInstance().writeValueAsBytes(tableData);
        } catch (IOException e) {
            throw AssertUtil.isTrue(true)
                .i18n("table.adapter.write.error", e.getMessage())
                .errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
                .cause(e)
                .build();
        }
    }

    /**
     * 实际 JSON 解析逻辑
     *
     * @param stream 输入流
     * @return TableData 实例
     * @throws IOException 解析异常
     */
    private TableData doAdapt(InputStream stream) throws IOException {
        ObjectMapper mapper = JsonUtils.getMapperInstance();
        JsonNode root = mapper.readTree(new InputStreamReader(stream, StandardCharsets.UTF_8));

        TableData.Builder builder = TableData.builder();

        JsonNode tableIdNode = root.get("tableId");
        if (tableIdNode != null && !tableIdNode.isNull()) {
            builder.tableId(tableIdNode.asText());
        }

        JsonNode tableNameNode = root.get("tableName");
        if (tableNameNode != null && !tableNameNode.isNull()) {
            builder.tableName(tableNameNode.asText());
        }

        JsonNode columnsNode = root.get("columns");
        if (columnsNode != null && columnsNode.isArray()) {
            for (JsonNode colNode : columnsNode) {
                String name = colNode.has("name") ? colNode.get("name").asText() : null;
                int index = colNode.has("index") ? colNode.get("index").asInt() : 0;
                builder.addColumn(name, index);
            }
        }

        JsonNode rowsNode = root.get("rows");
        if (rowsNode != null && rowsNode.isArray()) {
            for (JsonNode rowNode : rowsNode) {
                List<String> cells = new ArrayList<>();
                JsonNode cellsNode = rowNode.get("cells");
                if (cellsNode != null && cellsNode.isArray()) {
                    for (JsonNode cellNode : cellsNode) {
                        cells.add(cellNode.isNull() ? null : cellNode.asText());
                    }
                }
                int rowIndex = rowNode.has("index") ? rowNode.get("index").asInt() : 0;
                builder.addRow(cells, rowIndex);
            }
        }

        return builder.build();
    }
}
