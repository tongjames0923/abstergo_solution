package com.tbs.logging.impl;

import com.tbs.common.data.ITableDataHandler;
import com.tbs.common.data.TableDataOperators;
import com.tbs.common.data.structs.TableData;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 远程表格数据操作器
 * <p>
 * 日志服务经 Dubbo 导出的 {@link ITableDataHandler} 实现：收到表格数据后调用
 * {@link TableDataOperators} 简单数据操作库，将结构摘要、数值列统计与行采样结果输出到本服务日志。
 * 该实现不会出现在 {@code IObjectProvider.getObjects} 的枚举结果中（Dubbo 不参与逐实例枚举），
 * 主应用亦未引用此服务，仅在使用方按坐标构建 Dubbo 集群引用时才会被调用。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class RemoteTableDataOperator implements ITableDataHandler {

    /**
     * 采样输出行数上限
     */
    private static final int SAMPLE_LIMIT = 3;

    /**
     * 获取表格数据处理器ID
     *
     * @return 处理器唯一标识
     */
    @Override
    public String getId() {
        return "RemoteTableDataOperator";
    }

    /**
     * 获取表格数据处理器名称
     *
     * @return 处理器展示名称
     */
    @Override
    public String getName() {
        return "远程表格数据操作器（结构摘要+数值统计+行采样）";
    }

    /**
     * 处理表格数据
     * <p>
     * 依次执行结构摘要、数值列统计、行采样三项操作，结果全部输出到日志。
     * </p>
     *
     * @param table 表格数据
     */
    @Override
    public void handle(TableData table) {
        log.info("RemoteTableDataOperator 结构摘要: {}", TableDataOperators.summary(table));
        TableDataOperators.numericStats(table).forEach((column, stats) -> {
            if (stats.getCount() == 0) {
                log.info("RemoteTableDataOperator 列[{}]: 非数值列", column);
                return;
            }
            log.info("RemoteTableDataOperator 数值列[{}]: count={} sum={} min={} max={} avg={}",
                column, stats.getCount(), stats.getSum(), stats.getMin(), stats.getMax(), stats.getAvg());
        });
        List<TableData.Row> sample = TableDataOperators.sampleRows(table, SAMPLE_LIMIT);
        log.info("RemoteTableDataOperator 采样前{}行: {}", sample.size(), sample);
    }
}
