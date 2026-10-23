package com.tbs.logging;

import com.tbs.async.annotation.EnableTbsAsyncPack;
import com.tbs.cache.annotation.EnableTbsCache;
import com.tbs.rabbitmq.annotation.EnableRabbitMq;
import com.tbs.redis.annotation.EnableTbsRedis;
import com.tbs.shared.annotation.EnableSharedLibBean;
import com.tbs.sql.annotation.core.EnableTbsSql;
import com.tbs.sql.annotation.dialect.UseMysql;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.mybatis.spring.annotation.MapperScan;

/**
 * 日志服务启动类
 * <p>
 * 该类是日志Dubbo服务的入口点，负责初始化Spring Boot应用程序并启用各种框架功能
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.tbs.logging"})
@EnableTbsSql
@EnableTbsAsyncPack
@EnableTbsCache
@EnableSharedLibBean
@UseMysql
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTbsRedis
@EnableDubbo
@EnableRabbitMq
@MapperScan("com.tbs.logging.mapper")
public class LoggingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoggingServiceApplication.class, args);
    }

}
