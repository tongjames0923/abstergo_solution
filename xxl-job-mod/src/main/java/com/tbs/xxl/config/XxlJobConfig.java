package com.tbs.xxl.config;

import cn.hutool.core.util.StrUtil;
import com.tbs.common.error.AssertUtil;
import com.tbs.xxl.api.IXXLJobsConfig;
import com.tbs.xxl.config.property.XxlJobProperty;
import com.tbs.xxl.executor.JsonJobExecutor;
import com.tbs.xxl.executor.RunningBackGroundExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j

@Configuration
public class XxlJobConfig {

    @Resource
    private XxlJobProperty property;

    @Bean
    @ConditionalOnMissingBean(IXXLJobsConfig.class)
    @ConditionalOnProperty(value = "tbs.xxl-job.enable", havingValue = "true", matchIfMissing = false)
    public IXXLJobsConfig xxlJobsConfig() {
        return new IXXLJobsConfig() {
            @Override
            public String adminAddress() {
                return property.getConfig().getAdminAddress();
            }

            @Override
            public String appName() {
                return property.getConfig().getAppName();
            }

            @Override
            public String address() {
                return property.getConfig().getAddress();
            }

            @Override
            public String ip() {
                return property.getConfig().getIp();
            }

            @Override
            public Integer port() {
                return property.getConfig().getPort();
            }

            @Override
            public String accessToken() {
                return property.getConfig().getAccessToken();
            }

            @Override
            public String logPath() {
                return property.getConfig().getLogPath();
            }

            @Override
            public Integer logRetentionsDays() {
                return property.getConfig().getLogRetentionsDays();
            }
        };
    }



    @Bean
    @ConditionalOnBean(IXXLJobsConfig.class)
    @ConditionalOnProperty(value = "tbs.xxl-job.enable", havingValue = "true", matchIfMissing = false)
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
            // 确保日志目录存在,避免XxlJobFileAppender.appendLog抛出IOException
            java.io.File logDir = new java.io.File(config.logPath());
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            xxlJobSpringExecutor.setLogPath(config.logPath());
        }
        if (null != config.logRetentionsDays()) {
            log.info("logRetentionsDays: {}", config.logRetentionsDays());
            xxlJobSpringExecutor.setLogRetentionDays(config.logRetentionsDays());
        }
        return xxlJobSpringExecutor;
    }

    @Bean
    @ConditionalOnMissingBean(RunningBackGroundExecutor.class)
    RunningBackGroundExecutor runningBackGroundExecutor() {
        return new RunningBackGroundExecutor();
    }

    @Bean
    @ConditionalOnBean(IXXLJobsConfig.class)
    @ConditionalOnProperty(value = "tbs.xxl-job.enable-json-job-executor", havingValue = "true", matchIfMissing = false)
    public JsonJobExecutor jsonJobExecutor() {
        return new JsonJobExecutor();
    }

}
