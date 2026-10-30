package com.tbs.common.data.impl;

import cn.hutool.core.text.csv.CsvReadConfig;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvWriter;
import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.data.ITableDataAdapter;
import com.tbs.common.data.structs.TableData;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV 格式表格数据适配器
 * <p>
 * CSV 格式约定：
 * <ul>
 *   <li>第一行为列名（header），后续行为数据行</li>
 *   <li>列序号 = 列位置（0-based）</li>
 *   <li>行序号 = 行位置（0-based，不含 header）</li>
 *   <li>tableId / tableName 在 CSV 中无对应，设为 null</li>
 * </ul>
 * <p>
 * 读写均使用 UTF-8 编码。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class CsvTableDataAdapter implements ITableDataAdapter {

    /**
     * 从输入流读取 CSV 并解析为 TableData
     *
     * @param stream 输入流，不能为 null
     * @return 不可变 TableData 实例
     */
    @Override
    public TableData adapt(InputStream stream) {
        AssertUtil.isTrue(stream == null).i18n("table.adapter.stream.null").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        try (CsvReader csvReader = new CsvReader(new InputStreamReader(stream, StandardCharsets.UTF_8),
            CsvReadConfig.defaultConfig())) {
            var rows = csvReader.read().getRows();
            AssertUtil.isTrue(rows == null || rows.isEmpty()).i18n("table.adapter.csv.header.missing")
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

            CsvRow header = rows.get(0);
            TableData.Builder builder = TableData.builder();
            for (int i = 0; i < header.size(); i++) {
                builder.addColumn(header.get(i), i);
            }

            for (int i = 1; i < rows.size(); i++) {
                CsvRow row = rows.get(i);
                builder.addRow(new ArrayList<>(row), i - 1);
            }

            return builder.build();
        } catch (Exception e) {
            if (e instanceof FormattedRuntimeException) {
                throw (FormattedRuntimeException)e;
            }
            throw AssertUtil.isTrue(true).i18n(CommonI18nConstant.TABLE_ADAPTER_CSV_PARSE_FAILED)
                .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 将 TableData 序列化为 CSV 字节数组
     *
     * @param tableData 表格数据，不能为 null
     * @return UTF-8 编码的 CSV 字节数组
     */
    @Override
    public byte[] write(TableData tableData) {
        AssertUtil.isTrue(tableData == null).i18n("table.adapter.write.data.null")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CsvWriter csvWriter = new CsvWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            List<TableData.Column> columns = tableData.getColumns();
            String[] headers = new String[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                headers[i] = columns.get(i).getName();
            }
            csvWriter.write(headers);

            List<TableData.Row> rows = tableData.getRows();
            for (TableData.Row row : rows) {
                List<String> cells = row.getCells();
                csvWriter.write(cells.toArray(new String[0]));
            }
        } catch (Exception e) {
            throw AssertUtil.isTrue(true).i18n(CommonI18nConstant.TABLE_ADAPTER_WRITE_FAILED)
                .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).cause(e).build();
        }
        return out.toByteArray();
    }
}
