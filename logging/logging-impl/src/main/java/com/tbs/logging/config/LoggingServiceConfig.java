package com.tbs.logging.config;

import com.tbs.app.service.log.ISystemLogService;
import com.tbs.common.data.ITableDataHandler;
import com.tbs.logging.core.ILogExtractor;
import com.tbs.logging.impl.DbLogExtractor;
import com.tbs.logging.impl.RemoteTableDataOperator;
import com.tbs.logging.impl.SimpleSystemLogServiceImpl;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingServiceConfig {
    @Bean
    public ILogExtractor logExtractor() {
        return new DbLogExtractor();
    }

    @Bean
    @DubboService
    public ISystemLogService logService() {
        return new SimpleSystemLogServiceImpl();
    }

    /**
     * 远程表格数据操作器
     * <p>
     * 导出 {@link ITableDataHandler} 供主应用经 IObjectProvider 多来源枚举发现，
     * 验证「本地 Bean + 远程 Dubbo 实例」统一处理链路。
     * </p>
     *
     * @return 表格数据处理器远程实现
     */
    @Bean
    @DubboService
    public ITableDataHandler remoteTableDataOperator() {
        return new RemoteTableDataOperator();
    }
}
