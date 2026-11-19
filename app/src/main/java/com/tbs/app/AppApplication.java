package com.tbs.app;

import com.tbs.async.annotation.EnableTbsAsyncPack;
import com.tbs.cache.annotation.EnableTbsCache;
import com.tbs.knife4j.annotation.UseKnife4jPack;
import com.tbs.oss.annotation.EnableTbsOss;
import com.tbs.push.annotation.EnableTbsPush;
import com.tbs.push.sse.annotation.EnableTbsPushSse;
import com.tbs.rabbitmq.annotation.EnableRabbitMq;
import com.tbs.redis.annotation.EnableTbsRedis;
import com.tbs.script.annotation.EnableScriptEngine;
import com.tbs.shared.annotation.EnableSharedLibBean;
import com.tbs.sql.annotation.core.SqlEntityScan;
import com.tbs.struct.tree.closure.annotation.UseClosureTableTree;
import com.tbs.view.annotation.EnableViewControl;
import com.tbs.web.annotation.EnableWebPack;
import com.tbs.xxl.annotation.UseXxlJob;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 应用程序启动类
 * <p>
 * 该类是应用程序的入口点，负责初始化Spring Boot应用程序并启用各种框架功能
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@EnableWebPack
@SpringBootApplication(scanBasePackages = {"com.tbs.app"})
@EnableTbsAsyncPack
@EnableTbsCache
@EnableRabbitMq
@UseXxlJob
@UseKnife4jPack
@EnableViewControl
@EnableSharedLibBean
@UseClosureTableTree
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTbsRedis
@EnableTbsOss
@EnableDubbo
@EnableScriptEngine
@EnableTbsPush
@EnableTbsPushSse
@SqlEntityScan({"com.tbs.shared.pojo.entity", "com.tbs.struct.tree.closure.impl.entity"})
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

}
