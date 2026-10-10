package com.tbs.cdc.i18n;

import com.tbs.common.i18n.AbstractMessageSourceProvider;
import org.springframework.stereotype.Component;

/**
 * CDC模块消息源提供者
 * <p>
 * 注册CDC运行时的国际化资源文件（i18n/messages-cdc），供断言异常文案统一取用。
 * 仅在CDC启用时随组件一并装配。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class CdcMessageSourceProvider extends AbstractMessageSourceProvider {

    @Override
    public String getBasename() {
        return "i18n/messages-cdc";
    }

    @Override
    public int getOrder() {
        return -400;
    }
}
