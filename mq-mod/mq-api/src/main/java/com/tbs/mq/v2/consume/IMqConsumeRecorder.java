package com.tbs.mq.v2.consume;

import com.tbs.mq.v2.vo.MqConsumeVO;

import java.io.Serializable;
import java.util.List;

/**
 * 消费台账记录器接口
 * <p>
 * 逐条记录“消息 × 消费者”的每次分发结果（messageId + consumeId + 成败状态 + 起止时间 + 异常），
 * 是重试扫描判断回灌目标与消费者消费进度的依据；与消息主记录（{@code IMqRecorder}）分层：
 * 主记录管投递可靠性，台账管消费可靠性。实现类仅保留存取能力，不得内嵌调度/重投逻辑。
 * </p>
 *
 * @param <T> 业务负载类型
 */
public interface IMqConsumeRecorder<T extends Serializable> {

    /**
     * 批量记录消费台账
     * <p>
     * 一次分发后同一消息下各消费者的结果批处理落库，支持成否混合（SUCCESS/FAIL 同行表不互斥）。
     * </p>
     *
     * @param record 消费台账批次，不为空
     * @return 落库成功的行数
     */
    int record(List<MqConsumeVO<T>> record);
}
