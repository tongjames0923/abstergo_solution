package com.tbs.cdc.sqlserver.source;

import com.tbs.cdc.i18n.CdcI18nConstant;
import com.tbs.cdc.model.CdcEvent;
import com.tbs.cdc.property.CdcProperty;
import com.tbs.cdc.source.CdcSource;
import com.tbs.cdc.sqlserver.config.DebeziumConfigFactory;
import com.tbs.cdc.sqlserver.converter.DebeziumEventConverter;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 基于Debezium嵌入式引擎的SQL Server变更捕获源
 * <p>
 * 封装{@link DebeziumEngine}读取SQL Server CDC变更表（依赖目标库表已启用CDC），将每个批次事件经
 * {@link DebeziumEventConverter}转换为{@link CdcEvent}后回调上报。引擎运行在独立单线程
 * 执行器上（Debezium要求单线程顺序消费），位点由{@code FileOffsetBackingStore}持久化。
 * 停止时走{@link DebeziumEngine#close()}优雅关闭协议，避免位点丢失。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "tbs.cdc", name = "enable", havingValue = "true")
public class DebeziumCdcSource implements CdcSource {

    /**
     * 捕获源类型标识
     */
    private static final String SOURCE_TYPE = "sqlserver";

    @Resource
    private CdcProperty cdcProperty;

    @Resource
    private DebeziumConfigFactory debeziumConfigFactory;

    @Resource
    private DebeziumEventConverter debeziumEventConverter;

    /**
     * Debezium引擎实例
     */
    private DebeziumEngine<ChangeEvent<String, String>> engine;

    /**
     * 引擎独立单线程执行器
     */
    private ExecutorService engineExecutor;

    /**
     * 运行状态标志
     */
    private volatile boolean running = false;

    /**
     * 启动捕获：装配Debezium引擎并在独立线程运行，批次事件转换后回调上报
     *
     * @param handler 变更事件处理器
     */
    @Override
    public synchronized void start(Consumer<CdcEvent> handler) {
        if (running) {
            log.warn("DebeziumCdcSource(SQL Server)已在运行，忽略重复启动");
            return;
        }
        Properties properties = debeziumConfigFactory.build(cdcProperty);
        this.engineExecutor = Executors.newSingleThreadExecutor(engineThreadFactory());
        DebeziumEngine.ChangeConsumer<ChangeEvent<String, String>> consumer =
            (records, committer) -> handleBatch(records, committer, handler);
        this.engine = DebeziumEngine.create(Json.class)
            .using(properties)
            .notifying(consumer)
            .build();
        this.engineExecutor.submit(engine);
        this.running = true;
        log.info("DebeziumCdcSource(SQL Server)已启动, server-name:{}", cdcProperty.getSqlserver().getServerName());
    }

    /**
     * 处理一批变更事件：逐条转换上报，成功后标记位点，整批完成后提交
     * <p>
     * 单条转换/分发异常仅记录ERROR并跳过该条（仍标记已处理推进位点），避免毒事件阻塞复制流；
     * Debezium捕获层的连接类异常由引擎内部重连重试机制处理。
     * </p>
     *
     * @param batch     变更事件批次
     * @param committer 批次位点提交器
     * @param handler   变更事件处理器
     * @throws InterruptedException 处理被中断
     */
    private void handleBatch(Iterable<ChangeEvent<String, String>> batch,
        DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> committer, Consumer<CdcEvent> handler)
        throws InterruptedException {
        for (ChangeEvent<String, String> record : batch) {
            try {
                CdcEvent event = debeziumEventConverter.convert(record);
                if (event != null) {
                    handler.accept(event);
                }
            } catch (Exception e) {
                log.error("Debezium变更事件处理失败, 跳过并推进位点, key:{}", record.key(), e);
            } finally {
                // 至少一次语义：事件成功进入分发管道后标记处理，随批次结束提交位点
                committer.markProcessed(record);
            }
        }
        committer.markBatchFinished();
    }

    /**
     * 停止捕获：优雅关闭引擎并回收执行器线程
     */
    @Override
    public synchronized void stop() {
        if (engine == null) {
            running = false;
            return;
        }
        try {
            engine.close();
        } catch (IOException e) {
            throw AssertUtil.isTrue(true).cause(e).i18n(CdcI18nConstant.ENGINE_STOP_FAILED)
                .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).build();
        } finally {
            shutdownExecutor();
            running = false;
            log.info("DebeziumCdcSource(SQL Server)已停止");
        }
    }

    /**
     * 获取捕获源类型标识
     *
     * @return sqlserver
     */
    @Override
    public String getSourceType() {
        return SOURCE_TYPE;
    }

    /**
     * 判断捕获源是否处于运行状态
     *
     * @return true表示运行中
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 关闭引擎执行器，限时等待后强制终止
     */
    private void shutdownExecutor() {
        if (engineExecutor == null) {
            return;
        }
        engineExecutor.shutdown();
        try {
            if (!engineExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                engineExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            engineExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 构建引擎线程工厂，产出具名守护线程
     *
     * @return 线程工厂
     */
    private ThreadFactory engineThreadFactory() {
        return r -> {
            Thread t = new Thread(r, "cdc-debezium-engine-sqlserver");
            t.setDaemon(true);
            return t;
        };
    }
}
