package com.tbs.common.config;

import com.tbs.common.util.BeanUtil;
import com.tbs.common.error.ExceptionUtil;
import com.tbs.common.constant.HolderBeanNameConstant;
import com.tbs.common.holder.IContextHolder;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.holder.context.impl.LocalStoredMapImplContext;
import com.tbs.common.config.property.HolderProperty;
import com.tbs.common.error.AssertUtil;
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
        return new LocalStoredMapImplContext();
    }


    /**
     * 获取主上下文
     *
     * @return
     */
    @Bean(name = HolderBeanNameConstant.MAIN_CONTEXT_HOLDER)
    public IContextHolder getMainContextHolder() {
        return BeanUtil.buildBeanFromProperties(null, holderProperty.getMainContextHolderType(), (clazz) -> {
            AssertUtil.notNull(clazz, () -> ExceptionUtil.newException("构造类型不能为空"));
            try {
                return clazz.getConstructor().newInstance();
            } catch (Exception e) {
                AssertUtil.throwError(() -> ExceptionUtil.newException(e, "主要上下文环境持有器构建失败"));
            }
            return null;
        });
    }

}
