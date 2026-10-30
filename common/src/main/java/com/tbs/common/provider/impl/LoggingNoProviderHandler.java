package com.tbs.common.provider.impl;

import com.tbs.common.provider.INoProviderHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 日志无提供者处理
 *
 * @author abstergo
 */
@Slf4j
@Component
public class LoggingNoProviderHandler implements INoProviderHandler {
    /**
     * 无提供者处理
     *
     * @param business        业务线
     * @param ver             版本号
     * @param targetInterface 目标接口
     */
    @Override
    public void handle(String business, String ver, Class<?> targetInterface) {
        log.warn("No provider for business: [{}], ver: [{}], interface: [{}]", business, ver,
            targetInterface.getName());
    }
}
