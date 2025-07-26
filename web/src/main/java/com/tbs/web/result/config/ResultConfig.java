package com.tbs.web.result.config;

import com.tbs.common.util.BeanUtil;
import com.tbs.web.result.IAjaxBuilderProvider;
import com.tbs.web.result.IOnErrorAjaxProvider;
import com.tbs.web.result.IResultCreator;
import com.tbs.web.result.impl.DefaultResultCreateor;
import com.tbs.web.result.impl.SimpleBuilderProvider;
import com.tbs.web.result.impl.SimpleErrorAjaxProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class ResultConfig {

    @Resource
    private com.tbs.web.result.config.property.ResultProperty resultProperty;

    /**
     * 创建 AJAX 结果构建器提供者
     *
     * 使用 BeanUtil 根据配置构建结果构建器
     *
     * @return AJAX 结果构建器提供者
     */
    @Bean
    @ConditionalOnMissingBean(IAjaxBuilderProvider.class)
    public IAjaxBuilderProvider ajaxBuilderProvider() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new SimpleBuilderProvider();
        }, resultProperty.getResultBuilderProvier(), BeanUtil::buildByEmptyArgs);
    }

    /**
     * 创建错误结果构建器提供者
     *
     * 使用 BeanUtil 根据配置构建错误结果构建器
     *
     * @return 错误结果构建器提供者
     */
    @Bean
    @ConditionalOnMissingBean(IOnErrorAjaxProvider.class)
    public IOnErrorAjaxProvider errorResultBuilderProvider() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new SimpleErrorAjaxProvider();
        }, resultProperty.getErrorResultBuilderProvier(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(IResultCreator.class)
    public IResultCreator resultCreator() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new DefaultResultCreateor();
        }, resultProperty.getResultCreator(), BeanUtil::buildByEmptyArgs);
    }
}
