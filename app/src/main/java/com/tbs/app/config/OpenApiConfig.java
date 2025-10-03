package com.tbs.app.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 配置类
 * 
 * 替代 Springfox Swagger 的配置，提供与 Spring Boot 2.7+ 完全兼容的 API 文档功能
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tbsOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("TBS Solution API")
                        .description("TBS 后端服务 API 文档")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0")
                                .url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("TBS 解决方案文档")
                        .url("https://github.com/abstergo/abstergo-solution"));
    }
}