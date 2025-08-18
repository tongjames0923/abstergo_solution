package com.tbs.app;

import com.tbs.sql.annotation.EnableTbsSqlUtils;
import com.tbs.sql.annotation.dialect.MYSQL;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * 启动类
 *
 * @author abstergo
 */
@SpringBootApplication(scanBasePackages = "com.tbs.**")
@EnableTbsSqlUtils
@MYSQL
@MapperScan(basePackages = "com.tbs.app.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true,exposeProxy = true)
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

}
