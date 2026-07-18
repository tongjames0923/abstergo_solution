package com.tbs.web.config;

import com.tbs.common.i18n.AbstractMessageSourceProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Web 模块消息源提供者
 * <p>
 * 自动注册 web 模块的消息资源文件：messages-web.properties
 *
 * @author abstergo
 */
@Component
public class WebMessageSourceProvider extends AbstractMessageSourceProvider {

    @Override
    @NotNull
    public String getBasename() {
        return "messages-web";
    }

    @Override
    public int getOrder() {
        // Web 模块优先级
        return -200;
    }
}