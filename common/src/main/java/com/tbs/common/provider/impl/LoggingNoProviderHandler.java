package com.tbs.common.provider.impl;

import com.tbs.common.provider.INoProviderHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingNoProviderHandler implements INoProviderHandler {
    @Override
    public void handle(Class<?> clazz, String method, Object[] args) {
        log.warn("No provider for service: {} method: {} args: {}", clazz, method, args);
    }
}
