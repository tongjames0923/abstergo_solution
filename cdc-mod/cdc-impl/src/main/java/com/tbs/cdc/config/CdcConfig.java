package com.tbs.cdc.config;

import com.tbs.cdc.bus.CdcEventBus;
import com.tbs.cdc.property.CdcProperty;
import com.tbs.cdc.publisher.CdcEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CDC运行时装配配置
 * <p>
 * 总开关{@code tbs.cdc.enable=true}时激活：绑定{@link CdcProperty}配置属性，并将事件总线
 * 作为{@link CdcEventPublisher}契约对外暴露，供捕获源以外的其他来源复用分发管道。
 * 关闭时本配置与全部运行期组件均不装配，零Bean、零线程占用。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(prefix = "tbs.cdc", name = "enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CdcProperty.class)
public class CdcConfig {

    /**
     * 对外暴露事件发布契约，委托进程内事件总线实现
     *
     * @param cdcEventBus 进程内事件总线
     * @return 事件发布契约Bean
     */
    @Bean
    public CdcEventPublisher cdcEventPublisher(CdcEventBus cdcEventBus) {
        return cdcEventBus;
    }
}
