package com.tbs.logging.impl;

import cn.hutool.core.collection.CollUtil;
import com.tbs.logging.form.LoggingRecordForm;
import com.tbs.logging.rabbitmq.AbstractRabbitMqLogRecordWriter;
import com.tbs.rabbitmq.support.MqConsumerProperties;
import com.tbs.shared.mapper.SystemOperateMapper;
import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 数据库日志写入器
 * <p>
 * 通过RabbitMQ消费日志消息，并批量写入数据库。 采用本地缓冲+批量写入的模式，提升日志写入性能。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
public class DbLogWriter extends AbstractRabbitMqLogRecordWriter {

    /**
     * 批量写入的批次大小
     */
    private static final int BATCH_SIZE = 100;

    @Resource
    private SystemOperateMapper mapper;

    /**
     * 本地缓冲队列，用于暂存待写入的日志记录
     */
    private final BlockingQueue<SystemOperateRecordEntity> bufferQueue = new LinkedBlockingQueue<>(100);

    @Override
    protected MqConsumerProperties getProperties() {
        return MqConsumerProperties.builder("systemLog").exchange("tbs.log.queue").routingKey("systemLog")
            .prefetchCount(250).retryEnabled(false).build();
    }

    @Override
    protected void handleMessage( LoggingRecordForm message) throws Exception {
        // 检查是否为刷新标志
        if (message.getFlushFlag() != null && message.getFlushFlag() == 1) {
            flushBuffer();
            return;
        }

        // 转换为实体并加入缓冲队列
        SystemOperateRecordEntity entity = convertToEntity(message);
        if (entity != null) {
            entity.setSystemLogFlag(1);
            boolean offered = bufferQueue.offer(entity);
            if (!offered) {
                log.warn("日志缓冲队列已满，强制刷新");
                flushBuffer();
                bufferQueue.offer(entity);
            }
        }
    }

    /**
     * 刷新缓冲区，批量写入数据库
     */
    private void flushBuffer() {
        if (bufferQueue.isEmpty()) {
            return;
        }

        List<SystemOperateRecordEntity> batch = new ArrayList<>(BATCH_SIZE);
        bufferQueue.drainTo(batch, BATCH_SIZE);

        if (CollUtil.isEmpty(batch)) {
            return;
        }

        try {
            mapper.insertList(batch);
            log.debug("批量写入日志记录成功，数量: {}", batch.size());
        } catch (Exception e) {
            log.error("批量写入日志记录失败，数量: {}", batch.size(), e);
            // 失败时尝试逐条写入
            for (SystemOperateRecordEntity entity : batch) {
                try {
                    mapper.insertSelective(entity);
                } catch (Exception ex) {
                    log.error("单条日志写入失败", ex);
                }
            }
        }
    }

    /**
     * 将LoggingRecordForm转换为SystemOperateRecordEntity
     *
     * @param form 日志记录表单
     * @return 实体对象
     */
    private SystemOperateRecordEntity convertToEntity(LoggingRecordForm form) {
        if (form == null) {
            return null;
        }

        SystemOperateRecordEntity entity = new SystemOperateRecordEntity();
        entity.setOperateType(form.getOperateType());
        entity.setOperateContent(form.getOperateDesc());
        entity.setOperateUserName(form.getOperateUser());
        entity.setOperateIp(form.getOperateIp());
        entity.setOperateTime(form.getOperateTime());
        entity.setOperateResultJson(form.getOperateResult());
        entity.setOperateParamJson(form.getOperateParam());
        entity.setOperateError(form.getErrorMessage());
        entity.setOperateErrorLevel(form.getErrorCode());
        entity.setOperateUrl(form.getOperateUrl());
        entity.setMethod(form.getMethod());
        entity.setTraceId(form.getTraceId());
        // 计算耗时
        if (form.getOperateTime() != null && form.getOperateFinishTime() != null) {
            entity.setCostTime(form.getOperateFinishTime().getTime() - form.getOperateTime().getTime());
        }

        return entity;
    }
}
