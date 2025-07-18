package com.tbs.app;

import com.tbs.app.annoation.UseSystemApp;
import com.tbs.sql.annotation.EnableTbsSqlUtils;
import com.tbs.sql.annotation.dialect.MYSQL;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * 启动类
 *
 * @author abstergo
 */
@SpringBootApplication(scanBasePackages = "com.tbs.**")
@UseSystemApp
@EnableTbsSqlUtils
@MYSQL
@MapperScan(basePackages = "com.tbs.app.mapper")
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

}
