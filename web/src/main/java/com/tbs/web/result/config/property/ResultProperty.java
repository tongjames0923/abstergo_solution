package com.tbs.web.result.config.property;

import com.tbs.web.result.IAjaxBuilderProvider;
import com.tbs.web.result.IOnErrorAjaxProvider;
import com.tbs.web.result.IResultCreator;
import com.tbs.web.result.impl.DefaultErrorAjaxProvider;
import com.tbs.web.result.impl.DefaultResultCreateor;
import com.tbs.web.result.impl.SimpleBuilderProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 统一响应结果配置属性
 * <p>
 * 绑定前缀 {@code tbs.result}，用于在配置中心指定结果包装各组件的实现类（默认均为项目内置实现）。
 * </p>
 *
 * @author tongj
 */
@Component
@Data
@ConfigurationProperties("tbs.result")
public class ResultProperty {

    /**
     * 是否启用统一响应结果（默认 true）
     */
    private boolean enable = true;

    /**
     * 结果创建者实现类，默认 {@link DefaultResultCreateor}
     */
    private Class<? extends IResultCreator> resultCreator = DefaultResultCreateor.class;

    /**
     * 结果构建器提供者实现类，默认 {@link SimpleBuilderProvider}
     */
    private Class<? extends IAjaxBuilderProvider> resultBuilderProvier = SimpleBuilderProvider.class;

    /**
     * 默认错误结果构建器实现类，默认 {@link DefaultErrorAjaxProvider}
     */
    private Class<? extends IOnErrorAjaxProvider> errorResultBuilderProvier = DefaultErrorAjaxProvider.class;
}
