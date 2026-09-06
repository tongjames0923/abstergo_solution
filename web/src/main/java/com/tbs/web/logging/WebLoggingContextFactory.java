package com.tbs.web.logging;

import com.tbs.logging.core.ILoggingContextCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Web 日志上下文工厂
 * <p>
 * web 模块侧的日志上下文装配入口：产出并注册 Web 来源的
 * {@link ILoggingContextCreator}，供消费方（日志切面等）经
 * {@code ILoggingContextResolver} 自动定位。应用可通过注册同名
 * Bean（"webLoggingContextCreator"）替换默认实现。
 * </p>
 *
 * @author abstergo
 * @see WebLoggingContextCreator
 */
@Configuration
public class WebLoggingContextFactory {

    /**
     * 装配 Web 来源日志上下文创建器
     *
     * @return Web 来源日志上下文创建器
     */
    @Bean
    @ConditionalOnMissingBean(name = "webLoggingContextCreator")
    public ILoggingContextCreator webLoggingContextCreator() {
        return new WebLoggingContextCreator();
    }
}
