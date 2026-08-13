package com.tbs.mq.api.consumer;

import com.tbs.mq.api.exception.MqBusinessException;
import com.tbs.mq.api.exception.MqConsumeException;
import com.tbs.mq.api.exception.MqDeadLetterException;
import com.tbs.mq.api.exception.MqRetryableException;
import com.tbs.mq.api.retry.IFailedMessageStore;

/**
 * 消息处理分派契约
 * <p>
 * 消费链路的串联点：一条消息从"进入消费者"到"确认/失败"的完整处理约定。 实现方（消费基类）必须按以下顺序编排：
 * <ol>
 *     <li>通过{@link #resolveIdempotentKey(Object)}解析幂等键（幂等开启时），
 *         配合IIdempotentStore完成重复消息拦截</li>
 *     <li>调用{@link #dispatch(Object)}执行业务处理</li>
 *     <li>处理失败时调用{@link #onFailure(Object, Throwable)}，
 *         并按重试策略决定入库（经{@link #getFailedMessageStore()}）或进入死信流程</li>
 * </ol>
 * </p>
 *
 * @param <T> 消息体类型
 * @author tongj
 * @since 1.0.0
 */
public interface IMessageDispatcher<T> {

    /**
     * 分派处理一条消息
     * <p>
     * 处理失败的异常类型直接决定后续策略：
     * <ul>
     *     <li>{@link MqRetryableException} — 入库延迟重投递</li>
     *     <li>{@link MqBusinessException} — 不重试，按配置丢弃或死信留痕</li>
     *     <li>{@link MqDeadLetterException} — 跳过重试直接进死信</li>
     *     <li>其他异常 — 按{@link com.tbs.common.error.ErrorLevelEnum}级别兼容判定</li>
     * </ul>
     * </p>
     *
     * @param message 反序列化后的消息体
     * @throws Exception 处理异常，异常类型即处理策略，参见{@link MqConsumeException}
     */
    void dispatch(T message) throws Exception;

    /**
     * 消息处理失败回调
     * <p>
     * 在重试/死信决策之前执行，默认仅记录日志， 实现方可覆盖为告警、留痕等自定义逻辑。回调自身必须异常安全。
     * </p>
     *
     * @param message 处理失败的消息体
     * @param error   异常信息
     */
    void onFailure(T message, Throwable error);

    /**
     * 解析消息的幂等键
     * <p>
     * 幂等键来源优先级：实现方提供的业务键 → 消息messageId → 消息内容摘要。 返回null表示该消息不参与幂等控制。
     * </p>
     *
     * @param message 消息体
     * @return 幂等键，null表示跳过幂等
     */
    String resolveIdempotentKey(T message);

    /**
     * 获取失败消息存储
     * <p>
     * 系统异常且允许重试时，消息快照经此存储持久化，等待调度重投递。
     * </p>
     *
     * @return 失败消息存储，未配置重试存储时为null
     */
    IFailedMessageStore getFailedMessageStore();

}
