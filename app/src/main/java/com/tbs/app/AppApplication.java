package com.tbs.app;

import com.tbs.async.annotation.EnableTbsAsyncPack;
import com.tbs.cache.annotation.EnableTbsCache;
import com.tbs.rabbitmq.annotation.EnableRabbitMq;
import com.tbs.redis.annotation.EnableTbsRedis;
import com.tbs.sql.annotation.EnableTbsSqlUtils;
import com.tbs.sql.annotation.dialect.MYSQL;
import com.tbs.view.annotation.EnableViewControl;
import com.tbs.web.annotation.EnableWebPack;
import com.tbs.xxl.annotation.UseXxlJob;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * 启动类
 *
 * @author abstergo
 */
@EnableWebPack
@SpringBootApplication(scanBasePackages = {"com.tbs.app", "com.tbs.shared", "com.tbs.struct"})
@EnableTbsSqlUtils
@EnableTbsAsyncPack
@EnableTbsCache
@EnableRabbitMq
@UseXxlJob
@EnableViewControl
@MYSQL
@MapperScan(basePackages = "com.tbs.shared.mapper")
@EntityScan({"com.tbs.shared.pojo.entity", "com.tbs.shared.pojo.entity.view"})
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTbsRedis
@EnableDubbo
//@EnableTrace
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

}
