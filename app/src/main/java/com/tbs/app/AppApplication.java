package com.tbs.app;

import com.tbs.async.annotation.EnableTbsAsyncPack;
import com.tbs.cache.annotation.EnableTbsCache;
import com.tbs.rabbitmq.annotation.EnableRabbitMq;
import com.tbs.redis.annotation.EnableTbsRedis;
import com.tbs.shared.annotation.EnableSharedLibBean;
import com.tbs.sql.annotation.EnableTbsSqlUtils;
import com.tbs.sql.annotation.dialect.MYSQL;
import com.tbs.struct.tree.closure.annotation.UseClosureTableTree;
import com.tbs.view.annotation.EnableViewControl;
import com.tbs.web.annotation.EnableWebPack;
import com.tbs.xxl.annotation.UseXxlJob;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 启动类
 *
 * @author abstergo
 */
@EnableWebPack
@SpringBootApplication(scanBasePackages = {"com.tbs.app"})
@EnableTbsSqlUtils
@EnableTbsAsyncPack
@EnableTbsCache
@EnableRabbitMq
@UseXxlJob
@EnableViewControl
@EnableSharedLibBean
@MYSQL
@UseClosureTableTree
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTbsRedis
@EnableDubbo
//@EnableTrace
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

}
