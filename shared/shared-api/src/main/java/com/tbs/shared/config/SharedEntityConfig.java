package com.tbs.shared.config;

import com.tbs.common.i18n.AbstractMessageSourceProvider;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.mybatis.spring.annotation.MapperScan;

/**
 * 配置实体类扫描
 *
 * @author tongj
 */
@Configuration
@MapperScan(basePackages = "com.tbs.shared.mapper")
@EntityScan({"com.tbs.shared.pojo.entity", "com.tbs.shared.pojo.entity.view"})
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
