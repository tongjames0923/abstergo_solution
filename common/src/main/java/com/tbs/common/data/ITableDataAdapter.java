package com.tbs.common.data;

import com.tbs.common.data.structs.TableData;

import java.io.InputStream;

/**
 * 表格数据适配器
 * <p>
 * 定义表格数据的读取（adapt）与写入（write）契约，
 * 支持不同格式（JSON、CSV 等）的实现。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ITableDataAdapter {

    /**
     * 从输入流读取并解析表格数据
     *
     * @param stream 输入流，不能为 null
     * @return 解析后的 TableData 实例
     */
    TableData adapt(InputStream stream);

    /**
     * 将表格数据序列化为字节数组
     *
     * @param tableData 表格数据，不能为 null
     * @return 序列化后的 UTF-8 编码字节数组
     */
    byte[] write(TableData tableData);
}
