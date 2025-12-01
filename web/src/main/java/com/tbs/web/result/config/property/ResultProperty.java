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
 * @author tongj
 */
@Component
@Data
@ConfigurationProperties("tbs.result")
public class ResultProperty {

    /**
     * 是否启用
     */
    private boolean enable = true;

    /**
     * 创建结果对象
     */
    private Class<? extends IResultCreator> resultCreator = DefaultResultCreateor.class;

    /**
     * 返回结果构建器
     */
    private Class<? extends IAjaxBuilderProvider> resultBuilderProvier = SimpleBuilderProvider.class;

    /**
     * 错误结果构建器
     */
    private Class<? extends IOnErrorAjaxProvider> errorResultBuilderProvier = DefaultErrorAjaxProvider.class;
}
