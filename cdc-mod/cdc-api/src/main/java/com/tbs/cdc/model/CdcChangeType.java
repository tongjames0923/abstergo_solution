package com.tbs.cdc.model;

/**
 * 数据库变更类型枚举
 * <p>
 * 描述CDC捕获源上报的一次数据变更的操作语义。跨数据库保持一致：
 * 各数据库实现负责将自身的变更操作码映射到本枚举。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum CdcChangeType {

    /**
     * 插入操作：新增一行数据
     */
    CREATE,

    /**
     * 更新操作：修改已有行的部分或全部列
     */
    UPDATE,

    /**
     * 删除操作：删除已有行
     */
    DELETE,

    /**
     * 读取操作（快照读）
     * <p>
     * 预留给全量快照阶段读取的存量数据。基于binlog日志流的机制无法捕获常规SELECT查询，
     * 本枚举值仅用于快照初始化等场景标识，正常运行期不会产生。
     * </p>
     */
    READ,
}
