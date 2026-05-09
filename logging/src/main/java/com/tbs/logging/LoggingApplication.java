package com.tbs.logging;

import com.tbs.async.annotation.EnableTbsAsyncPack;
import com.tbs.cache.annotation.EnableTbsCache;
import com.tbs.rabbitmq.annotation.EnableRabbitMq;
import com.tbs.redis.annotation.EnableTbsRedis;
import com.tbs.shared.annotation.EnableSharedLibBean;
import com.tbs.sql.annotation.EnableTbsSqlUtils;
import com.tbs.sql.annotation.dialect.MYSQL;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableDubbo
@EnableTbsSqlUtils
@EnableTbsAsyncPack
@EnableTbsCache
@EnableRabbitMq
@EnableSharedLibBean
@MYSQL
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTbsRedis
public class LoggingApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoggingApplication.class, args);
    }

}
