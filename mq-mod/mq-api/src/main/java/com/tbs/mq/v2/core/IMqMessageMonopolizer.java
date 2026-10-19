package com.tbs.mq.v2.core;

import com.tbs.mq.v2.vo.MqMessageIncomeVO;

import java.io.Serializable;

/**
 * 消息抢占者
 * <p>
 * 单飞保护以消息为粒度：同一 messageId 同一时刻仅允许一个处理者持有并分发，
 * 多实例部署时防止同一消息被并行重处理；重试回灌同样需先抢占，保证旧处理者与回灌者不并发。
 * 抢占失败方不得释放他人持有的占有。抢占/释放均传入上下文，与记录层保持同一队列级作用域。
 * </p>
 */
public interface IMqMessageMonopolizer {
    /**
     * 抢占消息
     *
     * @param context 当前上下文（队列级作用域）
     * @param message 待抢占的消息主记录VO
     * @return true表示抢占成功，调用方负责处理完成后 release；false表示他人持有，不得触碰 release
     */
    <R, T extends Serializable> boolean monopolize(IMqContext<R, T> context, MqMessageIncomeVO<R, T> message);

    /**
     * 释放消息的占有，仅限抢占成功的那个持有者调用
     *
     * @param context 当前上下文（队列级作用域）
     * @param message 已分发完成的消息主记录VO
     * @return 释放后的消息主记录VO
     */
    <R, T extends Serializable> MqMessageIncomeVO<R, T> release(IMqContext<R, T> context, MqMessageIncomeVO<R, T> message);

}
