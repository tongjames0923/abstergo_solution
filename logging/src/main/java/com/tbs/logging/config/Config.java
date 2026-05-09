package com.tbs.logging.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.shared.interfaces.log.ISystemOperateLogSender;
import com.tbs.shared.interfaces.log.ISystemOperateLogViewService;
import com.tbs.shared.interfaces.log.impl.NoViewLogServiceImpl;
import com.tbs.shared.interfaces.log.impl.SimpleLoggerSender;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class Config {

    @Resource
    private LoggingProperty loggingProperty;

    @Bean
    @DubboService
    ISystemOperateLogSender logSender() {
        return BeanUtil.buildBeanFromProperties(SimpleLoggerSender::new, loggingProperty.getSystemOperateLogSender(),
            BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @DubboService
    ISystemOperateLogViewService logViewService() {
        return BeanUtil.buildBeanFromProperties(NoViewLogServiceImpl::new,
            loggingProperty.getSystemOperateLogViewService(), BeanUtil::buildByEmptyArgs);
    }

}
