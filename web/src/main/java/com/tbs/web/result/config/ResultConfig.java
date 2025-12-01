package com.tbs.web.result.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.web.result.config.property.ResultProperty;
import com.tbs.web.result.IAjaxBuilderProvider;
import com.tbs.web.result.IOnErrorAjaxProvider;
import com.tbs.web.result.IResultCreator;
import com.tbs.web.result.impl.DefaultErrorAjaxProvider;
import com.tbs.web.result.impl.DefaultResultCreateor;
import com.tbs.web.result.impl.ReLoginErrorAjaxProvider;
import com.tbs.web.result.impl.SimpleBuilderProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 创建结果构建器提供者
 *
 * @author tongj
 */
@Configuration
@ConditionalOnProperty(prefix = "tbs.result", name = "enable", havingValue = "true", matchIfMissing = true)
public class ResultConfig {

    @Resource
    private ResultProperty resultProperty;

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
    public IOnErrorAjaxProvider commonErrorAjaxResultProvider() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new DefaultErrorAjaxProvider();
        }, resultProperty.getErrorResultBuilderProvier(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    public IOnErrorAjaxProvider reLoginErrorAjaxResultProvider() {
        return new ReLoginErrorAjaxProvider();
    }

    @Bean
    @ConditionalOnMissingBean(IResultCreator.class)
    public IResultCreator resultCreator() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new DefaultResultCreateor();
        }, resultProperty.getResultCreator(), BeanUtil::buildByEmptyArgs);
    }
}
