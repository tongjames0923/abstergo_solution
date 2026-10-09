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

import jakarta.annotation.Resource;

/**
 * 统一响应结果自动配置
 * <p>
 * 在 {@code tbs.result.enable=true}（默认开启）时装配结果包装相关 Bean：构建器提供者、错误结果提供者、
 * 结果创建者。均带 {@code @ConditionalOnMissingBean}，允许业务方自行覆盖；各实现类可由 {@link ResultProperty} 动态指定。
 * </p>
 *
 * @author tongj
 */
@Configuration
@ConditionalOnProperty(prefix = "tbs.result", name = "enable", havingValue = "true", matchIfMissing = true)
public class ResultConfig {

    @Resource
    private ResultProperty resultProperty;

    /**
     * 创建 AJAX 结果构建器提供者（可被自定义实现覆盖）
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
     * 创建默认错误结果构建器提供者（兜底，低优先级）
     *
     * @return 默认错误结果构建器提供者
     */
    @Bean
    @ConditionalOnMissingBean(IOnErrorAjaxProvider.class)
    public IOnErrorAjaxProvider commonErrorAjaxResultProvider() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new DefaultErrorAjaxProvider();
        }, resultProperty.getErrorResultBuilderProvier(), BeanUtil::buildByEmptyArgs);
    }

    /**
     * 注册「重新登录」错误结果提供者（固定存在，供 ReLoginRequestException 专用）
     *
     * @return 重新登录错误结果提供者
     */
    @Bean
    public IOnErrorAjaxProvider reLoginErrorAjaxResultProvider() {
        return new ReLoginErrorAjaxProvider();
    }

    /**
     * 创建默认结果创建者（可被自定义实现覆盖）
     *
     * @return 结果创建者
     */
    @Bean
    @ConditionalOnMissingBean(IResultCreator.class)
    public IResultCreator resultCreator() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new DefaultResultCreateor();
        }, resultProperty.getResultCreator(), BeanUtil::buildByEmptyArgs);
    }
}
