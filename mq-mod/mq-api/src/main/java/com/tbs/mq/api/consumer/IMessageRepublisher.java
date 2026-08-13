package com.tbs.mq.api.consumer;

import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.mq.model.FailedMessage;

/**
 * 失败消息重投递契约
 * <p>
 * 重投递链路的串联点：将{@link IFailedMessageStore}中到期的
 * {@link FailedMessage}还原为原始消息，递增重试次数头后经IMqProducer重新发出。
 * 实现方必须保证：
 * <ul>
 *     <li>重投递时保留原messageId，维持消费端幂等键连续</li>
 *     <li>重投递成功后从存储中移除记录；失败则更新下次重试时间重新入库</li>
 * </ul>
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public interface IMessageRepublisher {

    /**
     * 重投递一条到期的失败消息
     *
     * @param failedMessage 从失败存储中取出的消息记录
     * @return true表示重投递成功（记录可移除）；false表示发送失败（记录需重新入库等待下轮）
     */
    boolean republish(FailedMessage failedMessage);

}
