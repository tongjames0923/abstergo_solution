package com.tbs.dic;

import com.tbs.async.annotation.EnableTbsAsyncPack;
import com.tbs.cache.annotation.EnableTbsCache;
import com.tbs.redis.annotation.EnableTbsRedis;
import com.tbs.shared.annotation.EnableSharedLibBean;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = "com.tbs.dic")
@EnableTbsAsyncPack
@EnableTbsCache
@EnableSharedLibBean
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTbsRedis
@EnableDubbo
@MapperScan("com.tbs.dic.mapper")
public class DicServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DicServiceApplication.class, args);
    }
}
