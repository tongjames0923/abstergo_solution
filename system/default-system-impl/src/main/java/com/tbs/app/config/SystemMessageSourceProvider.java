package com.tbs.app.config;

import com.tbs.common.i18n.AbstractMessageSourceProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * System 模块消息源提供者
 * <p>
 * 自动注册 system 模块的消息资源文件：messages-system.properties
 *
 * @author abstergo
 */
@Component
public class SystemMessageSourceProvider extends AbstractMessageSourceProvider {

    @Override
    
    public String getBasename() {
        return "messages-system";
    }

    @Override
    public int getOrder() {
        // 系统模块优先级
        return -500;
    }
}
