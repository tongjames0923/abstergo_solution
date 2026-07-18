package com.tbs.common.config;

import com.tbs.common.i18n.AbstractMessageSourceProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Common 模块消息源提供者
 * <p>
 * 自动注册 common 模块的消息资源文件：messages-common.properties
 *
 * @author abstergo
 */
@Component
public class CommonMessageSourceProvider extends AbstractMessageSourceProvider {

    @Override
    @NotNull
    public String getBasename() {
        return "messages-common";
    }

    @Override
    public int getOrder() {
        // 通用模块优先级最高
        return -1000;
    }
}