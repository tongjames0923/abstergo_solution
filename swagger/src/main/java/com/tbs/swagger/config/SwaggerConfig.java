package com.tbs.swagger.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.tbs.common.util.SpringUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.swagger.property.SwaggerProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.Resource;

/**
 * @author abstergo
 */
@EnableKnife4j
@Configuration
public class SwaggerConfig {

    @Resource
    private SwaggerProperty swaggerProperty;

    @Bean
    WebMvcConfigurer swaggerConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(final ResourceHandlerRegistry registry) {
                registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
                registry.addResourceHandler("/webjars/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/");
                WebMvcConfigurer.super.addResourceHandlers(registry);
            }
        };
    }

    @Bean
    @ConditionalOnProperty(name = "tbs.framework.swagger.show-document-url", havingValue = "true",
        matchIfMissing = true)
    ApplicationRunner showDocumentUrl() {
        String context = SpringUtil.getApplicationContext().getEnvironment().getProperty("server.servlet.context-path");
        context = StrUtil.isEmpty(context) ? "" : context;
        String port = SpringUtil.getApplicationContext().getEnvironment().getProperty("server.port");
        port = StringUtils.isEmpty(port) ? "8080"
            : SpringUtil.getApplicationContext().getEnvironment().getProperty("server.port");
        String finalContext = context, finalPort = port;
        return new ApplicationRunner() {
            private final Logger logger = LoggerFactory.getLogger(SwaggerConfig.class);

            @Override
            public void run(ApplicationArguments args) throws Exception {
                logger.info("visit: http://127.0.0.1:{}{}/doc.html", finalPort, finalContext);
            }
        };
    }

    @Bean("defaultApi2")
    Docket defaultApi2() {
        return new Docket(this.swaggerProperty.getDocumentationType()).apiInfo(
                new ApiInfoBuilder().title(this.swaggerProperty.getTitle())
                    .termsOfServiceUrl(this.swaggerProperty.getTermsOfService()).version(this.swaggerProperty.getVersion())
                    .contact(new Contact(this.swaggerProperty.getContact(), this.swaggerProperty.getContactUrl(),
                        this.swaggerProperty.getEmail())).description(this.swaggerProperty.getDescription())
                    .license(this.swaggerProperty.getLicense()).licenseUrl(this.swaggerProperty.getLicenseUrl()).build())
            .groupName(this.swaggerProperty.getGroupName()).select()
            .apis(RequestHandlerSelectors.basePackage(this.swaggerProperty.getBasePackage())).paths(
                StrUtil.isEmpty(this.swaggerProperty.getPathPattern()) ? PathSelectors.any()
                    : PathSelectors.regex(this.swaggerProperty.getPathPattern())).build();
    }
}
