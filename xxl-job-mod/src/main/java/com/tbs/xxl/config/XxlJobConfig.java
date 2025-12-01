package com.tbs.xxl.config;

import cn.hutool.core.util.StrUtil;
import com.tbs.common.error.AssertUtil;
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
        AssertUtil.hasText(config.adminAddress(), "管理器地址不能为空");
        xxlJobSpringExecutor.setAdminAddresses(config.adminAddress());
        log.info("xxl job admin server: {}", config.adminAddress());
        if (!StrUtil.isEmpty(config.appName())) {
            log.info("app name: {}", config.appName());
            xxlJobSpringExecutor.setAppname(config.appName());
        }
        if (!StrUtil.isEmpty(config.address())) {
            log.info("address: {}", config.address());
            xxlJobSpringExecutor.setAddress(config.address());
        }
        if (!StrUtil.isEmpty(config.ip())) {
            log.info("ip: {}", config.ip());
            xxlJobSpringExecutor.setIp(config.ip());
        }
        if (null != config.port()) {
            log.info("port: {}", config.port());
            xxlJobSpringExecutor.setPort(config.port());
        }
        if (!StrUtil.isEmpty(config.accessToken())) {
            log.info("access token: {}", config.accessToken());
            xxlJobSpringExecutor.setAccessToken(config.accessToken());
        }
        if (!StrUtil.isEmpty(config.logPath())) {
            log.info("log path: {}", config.logPath());
            xxlJobSpringExecutor.setLogPath(config.logPath());
        }
        if (null != config.logRetentionsDays()) {
            log.info("logRetentionsDays: {}", config.logRetentionsDays());
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
