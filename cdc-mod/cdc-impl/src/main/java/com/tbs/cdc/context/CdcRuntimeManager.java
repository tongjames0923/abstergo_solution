package com.tbs.cdc.context;

import com.tbs.cdc.bus.CdcEventBus;
import com.tbs.cdc.i18n.CdcI18nConstant;
import com.tbs.cdc.source.CdcSource;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * CDC运行时生命周期编排器
 * <p>
 * 以{@link SmartLifecycle}接入Spring容器启停：启动阶段发现唯一{@link CdcSource}捕获源，
 * 串联“捕获源 → 事件总线”管道并启动捕获；停止阶段严格遵循“先停捕获、再刷队列”的顺序，
 * 保证停止过程中不再产生新事件后方可排空分发队列，实现优雅关闭与位点安全。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "tbs.cdc", name = "enable", havingValue = "true")
public class CdcRuntimeManager implements SmartLifecycle {

    @Resource
    private CdcEventBus cdcEventBus;

    /**
     * 捕获源提供者，容器中应存在唯一CdcSource实现
     */
    private final ObjectProvider<CdcSource> sourceProvider;

    /**
     * 已解析的捕获源
     */
    private volatile CdcSource cdcSource;

    /**
     * 运行状态标志
     */
    private volatile boolean running = false;

    /**
     * 构造运行时编排器
     *
     * @param sourceProvider 捕获源提供者
     */
    public CdcRuntimeManager(ObjectProvider<CdcSource> sourceProvider) {
        this.sourceProvider = sourceProvider;
    }

    /**
     * 启动CDC运行时：解析唯一捕获源并串联总线开始捕获
     */
    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        CdcSource source = resolveSource();
        this.cdcSource = source;
        cdcEventBus.start();
        try {
            source.start(cdcEventBus::publish);
        } catch (Exception e) {
            cdcEventBus.stop();
            throw AssertUtil.isTrue(true).cause(e).i18n(CdcI18nConstant.ENGINE_START_FAILED)
                .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).build();
        }
        this.running = true;
        log.info("CDC运行时已启动, source-type:{}", source.getSourceType());
    }

    /**
     * 停止CDC运行时：先停捕获源，再刷停事件总线
     */
    @Override
    public synchronized void stop() {
        if (!running) {
            return;
        }
        this.running = false;
        if (cdcSource != null) {
            try {
                cdcSource.stop();
            } catch (Exception e) {
                log.error("CDC捕获源停止失败, source-type:{}", cdcSource.getSourceType(), e);
            }
        }
        cdcEventBus.stop();
        log.info("CDC运行时已停止");
    }

    /**
     * 判断运行时是否处于运行状态
     *
     * @return true表示已启动
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 声明最大启动相位：在所有普通Bean就绪后最后启动、容器关闭时最先停止，
     * 保证停止顺序为“先停CDC捕获再释放其他资源”
     *
     * @return 生命周期相位
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    /**
     * 解析容器中唯一的捕获源实现
     *
     * @return 唯一CdcSource实例
     */
    private CdcSource resolveSource() {
        List<CdcSource> sources = sourceProvider.orderedStream().toList();
        AssertUtil.isTrue(sources.isEmpty()).i18n(CdcI18nConstant.SOURCE_NOT_FOUND)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        AssertUtil.isTrue(sources.size() > 1).i18n(CdcI18nConstant.SOURCE_MULTIPLE, sources.size())
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        return sources.get(0);
    }
}
