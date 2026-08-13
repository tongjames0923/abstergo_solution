package com.tbs.mq.api.retry;

import com.tbs.mq.model.FailedMessage;

import java.util.Date;
import java.util.List;

/**
 * 失败消息存储契约
 * <p>
 * 承担"消费失败→持久化→到期重投递"链路中的存储职责，是重试机制可插拔的核心抽象：
 * 实现可以是Redis（ZSet按到期时间排序）、数据库表（状态+下次重试时间列）等任意介质。
 * </p>
 * <p>
 * 多实例部署要求：{@link #fetchDue(Date, int)} 必须具备"取出即锁定"的原子语义，
 * 即同一条到期记录只能被一个调度实例取走，避免重复重投递。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public interface IFailedMessageStore {

    /**
     * 保存失败消息记录
     * <p>
     * 记录不存在时新增；同一{@link FailedMessage#getId()}再次保存时视为更新
     * （用于重投递失败后调整下次重试时间重新入库的场景）。
     * </p>
     *
     * @param message 失败消息记录
     */
    void save(FailedMessage message);

    /**
     * 取出已到期的失败消息记录
     * <p>
     * 仅返回{@link FailedMessage#getNextRetryTime()}不晚于指定时间的记录，
     * 且取出动作必须原子地完成占用（如Redis的ZPOPMIN或数据库的条件UPDATE），
     * 保证多调度实例并发调用时不会取到同一条记录。
     * </p>
     *
     * @param now   到期判定时间点
     * @param limit 单次最大取出条数
     * @return 已到期的失败消息记录列表，无到期记录时返回空列表
     */
    List<FailedMessage> fetchDue(Date now, int limit);

    /**
     * 删除记录
     * <p>
     * 重投递成功后调用，彻底移除该失败消息记录。
     * </p>
     *
     * @param messageId 记录唯一标识
     */
    void remove(String messageId);

}
