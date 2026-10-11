package com.tbs.knife4j.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.tbs.common.util.base.CollUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * knife4j配置类
 * <p>
 * 该类配置了knife4j的基本属性，如标题、描述、版本号、许可证等
 * </p>
 *
 * @author abstergo
 */
@Configuration
@Slf4j
public class Knife4jConfig {

    /**
     * knife4j配置属性
     */
    @Resource
    private Knife4jProperty property;

    @Value("server.port")
    private String port;

    @Bean
    @ConditionalOnProperty(name = "tbs.knife4j.enable", havingValue = "true", matchIfMissing = true)
    public OpenAPI tbsOpenAPI() {
        if (property == null) {
            property = new Knife4jProperty();
        }
        var info = new Info();
        if (com.tbs.common.util.base.StrUtil.isNotEmpty(property.getTitle())) {
            info.title(property.getTitle());
        } else {
            info.title("TBS 后端服务 API 文档");
        }
        if (com.tbs.common.util.base.StrUtil.isNotEmpty(property.getDescription())) {
            info.description(property.getDescription());
        } else {
            info.description("TBS 后端服务 API 文档");
        }
        if (com.tbs.common.util.base.StrUtil.isNotEmpty(property.getVersion())) {
            info.version(property.getVersion());
        }
        {
            info.version("v1.0.0");
        }
        if (com.tbs.common.util.base.StrUtil.isNotEmpty(property.getLicense())) {
            info.license(new License().name(property.getLicense()).url(property.getLicenseUrl()));
        } else {
            info.license(new License().name("Apache 2.0").url("http://springdoc.org"));
        }
        OpenAPI openAPI = new OpenAPI().info(info);
        // 添加服务器配置
        if (CollUtil.isNotEmpty(property.getServers())) {
            List<Server> servers = new ArrayList<>();
            for (String serverUrl : property.getServers()) {
                servers.add(new Server().url(serverUrl));
            }
            openAPI.servers(servers);
        }
        if (property.isShowUrl()) {
            //输出openapi的url访问 地址
            if (CollUtil.isNotEmpty(property.getServers())) {
                log.info("openapi url: {}", property.getServers().get(0));
            } else {
                String context =
                    SpringUtil.getApplicationContext().getEnvironment().getProperty("server.servlet.context-path");
                context = StrUtil.isEmpty(context) ? "" : context;
                String port = SpringUtil.getApplicationContext().getEnvironment().getProperty("server.port");
                port = StringUtils.isEmpty(port) ? "8080" : port;
                log.info("visit: http://127.0.0.1:{}{}/doc.html", port, context);
                log.info("or visit: http://127.0.0.1:{}{}/swagger-ui/index.html", port, context);
            }
        }
        return openAPI;
    }
}
