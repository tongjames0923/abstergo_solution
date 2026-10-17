package com.tbs.mq.v2.consume;

import com.tbs.mq.v2.vo.MqConsumeVO;

import java.io.Serializable;
import java.util.List;

public interface IMqConsumeRecorder<T extends Serializable> {

    /**
     * 记录消费信息
     *
     * @param record
     * @return
     */
    int record(List<MqConsumeVO<T>> record);
}
