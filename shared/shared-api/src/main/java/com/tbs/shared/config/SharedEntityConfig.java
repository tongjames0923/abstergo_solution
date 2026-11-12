package com.tbs.shared.config;

import com.tbs.common.i18n.AbstractMessageSourceProvider;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置 Mapper 扫描与消息源（实体扫描由 jkit.sql.auto.packages 配置）
 *
 * @author tongj
 */
@Configuration
@MapperScan(basePackages = "com.tbs.shared.mapper")
public class SharedEntityConfig {
    @Bean
    public AbstractMessageSourceProvider sharedMessageSourceProvider() {
        return new AbstractMessageSourceProvider() {
            @Override
            public String getBasename() {
                return "messages-shared";
            }
        };
    }

}
