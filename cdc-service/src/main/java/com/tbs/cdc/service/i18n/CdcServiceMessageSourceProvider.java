package com.tbs.cdc.service.i18n;

import com.tbs.common.i18n.AbstractMessageSourceProvider;
import org.springframework.stereotype.Component;

/**
 * CDC服务模块消息源提供者
 * <p>
 * 注册CDC服务的国际化资源文件（i18n/messages-cdc-service），供断言异常文案统一取用。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class CdcServiceMessageSourceProvider extends AbstractMessageSourceProvider {

    @Override
    public String getBasename() {
        return "i18n/messages-cdc-service";
    }

    @Override
    public int getOrder() {
        return -400;
    }
}
