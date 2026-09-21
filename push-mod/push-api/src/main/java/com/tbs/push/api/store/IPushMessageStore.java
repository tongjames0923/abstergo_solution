package com.tbs.push.api.store;

import com.tbs.push.model.message.PushMessage;

import java.util.List;

/**
 * 推送消息源存储契约
 * <p>
 * 推送链路的消息暂存抽象，是"业务来源消息先入消息源、分发器再消费"架构的核心：
 * 业务消息经门面进入本存储，分发器批量取出后按协调器解析的目标完成投递。
 * 存储实现可以是内存（默认兜底）、Redis、数据库等任意介质。
 * </p>
 * <p>
 * 多实例部署要求：{@link #fetchPending(int)} 必须具备"取出即占用"的原子语义，
 * 同一条消息只被一个实例取走一次（投递一次即可，不要求取出后崩溃时的补发）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IPushMessageStore {

    /**
     * 消息入源
     * <p>
     * 消息缺少唯一标识或入库时间时由调用方前置补齐；存储满时的淘汰策略由实现决定。
     * </p>
     *
     * @param message 业务消息
     */
    void enqueue(PushMessage message);

    /**
     * 原子取出一批待分发消息
     * <p>
     * 取出即从待分发集合移除，保证同一消息不会被重复取出；无待分发消息时返回空列表。
     * </p>
     *
     * @param limit 单次最大取出条数
     * @return 待分发消息列表，不为null
     */
    List<PushMessage> fetchPending(int limit);

    /**
     * 标记消息已分发完成
     * <p>
     * "取出即占用"语义下内存实现可空操作；需要显式确认的持久化实现
     * （如数据库状态列）在本方法完成终态固化。空id静默跳过。
     * </p>
     *
     * @param messageId 消息唯一标识
     */
    void markDispatched(String messageId);

    /**
     * 统计待分发消息数量
     *
     * @return 待分发消息数量
     */
    int pendingCount();

}
