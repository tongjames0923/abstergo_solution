package com.tbs.xxl.config;

import cn.hutool.core.util.StrUtil;
import com.tbs.xxl.api.IXXLJobsConfig;
import com.tbs.xxl.executor.JsonJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@ConditionalOnProperty(value = "tbs.xxl-job.enable", havingValue = "true", matchIfMissing = false)
@Component
public class XxlJobConfig {
    @Bean
    @ConditionalOnBean(IXXLJobsConfig.class)
    public XxlJobSpringExecutor xxlJobSpringExecutor(final IXXLJobsConfig config) {
        log.info("xxl job config start");
        final XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        if (StrUtil.isEmpty(config.adminAddress())) {
            throw new RuntimeException("管理器地址错误");
        }
        xxlJobSpringExecutor.setAdminAddresses(config.adminAddress());
        log.info(String.format("xxl job admin server: %s", config.adminAddress()));
        if (!StrUtil.isEmpty(config.appName())) {
            log.info(String.format("app name: %s", config.appName()));
            xxlJobSpringExecutor.setAppname(config.appName());
        }
        if (!StrUtil.isEmpty(config.address())) {
            log.info(String.format("address: %s", config.address()));
            xxlJobSpringExecutor.setAddress(config.address());
        }
        if (!StrUtil.isEmpty(config.ip())) {
            log.info(String.format("ip: %s", config.ip()));
            xxlJobSpringExecutor.setIp(config.ip());
        }
        if (null != config.port()) {
            log.info(String.format("port: %s", config.port()));
            xxlJobSpringExecutor.setPort(config.port());
        }
        if (!StrUtil.isEmpty(config.accessToken())) {
            log.info(String.format("access token: %s", config.accessToken()));
            xxlJobSpringExecutor.setAccessToken(config.accessToken());
        }
        if (!StrUtil.isEmpty(config.logPath())) {
            log.info(String.format("log path: %s", config.logPath()));
            xxlJobSpringExecutor.setLogPath(config.logPath());
        }
        if (null != config.logRetentionsDays()) {
            log.info(String.format("logRetentionsDays: %s", config.logRetentionsDays()));
            xxlJobSpringExecutor.setLogRetentionDays(config.logRetentionsDays());
        }
        return xxlJobSpringExecutor;
    }

    @Bean
    @ConditionalOnProperty(value = "tbs.xxl-job.enable-json-job-executor", havingValue = "true", matchIfMissing = false)
    public JsonJobExecutor jsonJobExecutor() {
        return new JsonJobExecutor();
    }

}
