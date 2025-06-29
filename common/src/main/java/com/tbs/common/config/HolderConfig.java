package com.tbs.common.config;

import com.tbs.common.constant.HolderBeanNameConstant;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.holder.IContextHolder;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.holder.context.impl.LocalStoredContext;
import com.tbs.common.holder.context.impl.ThreadLocalStoredContext;
import com.tbs.common.config.property.HolderProperty;
import com.tbs.common.util.SpringUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 上下文持有者配置类
 *
 * @author abstergo
 */
@Configuration
public class HolderConfig {

    /**
     * 配置属性
     */
    @Resource
    private HolderProperty holderProperty;

    /**
     * 默认的存储上下文
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "tbs.holder.use-thread-local-stored-context", havingValue = "true",
        matchIfMissing = true)
    IContext defaultLocalStoredContext() {
        return new LocalStoredContext();
    }

    /**
     * 默认的存储上下文
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "tbs.holder.use-thread-local-stored-context", havingValue = "true")
    IContext defaultThreadLocalContext() {
        return new ThreadLocalStoredContext();
    }

    /**
     * 获取主上下文
     *
     * @return
     */
    @Bean(name = HolderBeanNameConstant.MAIN_CONTEXT_HOLDER)
    public IContextHolder getMainContextHolder() {
        return SpringUtil.buildBeanFromProperties(null, holderProperty.getMainContextHolderType(), (clazz) -> {
            if (clazz == null) {
                throw new FormattedRuntimeException("HolderProperty.mainContextHolderType is null");
            }
            try {
                return clazz.getConstructor().newInstance();
            } catch (Exception e) {
                throw new FormattedRuntimeException("HolderProperty.mainContextHolderType  Error to build bean");
            }
        });
    }

}
