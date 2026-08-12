package com.tbs.view.route.config;

import com.tbs.common.i18n.AbstractMessageSourceProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * View Route 模块消息源提供者
 * <p>
 * 自动注册 view-route 模块的消息资源文件：messages-view-route.properties
 *
 * @author abstergo
 */
@Component
public class ViewRouteMessageSourceProvider extends AbstractMessageSourceProvider {

    @Override
    
    public String getBasename() {
        return "messages-view-route";
    }

    @Override
    public int getOrder() {
        // 视图路由模块优先级
        return -600;
    }
}
