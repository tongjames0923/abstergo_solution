package com.tbs.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.tbs.sql.annotation.EnableTbsSqlUtils;
import com.tbs.sql.annotation.dialect.MYSQL;

import tk.mybatis.spring.annotation.MapperScan;

/**
 * 启动类
 *
 * @author abstergo
 */
@SpringBootApplication(scanBasePackages = "com.tbs.**")
@EnableTbsSqlUtils
@MYSQL
@MapperScan(basePackages = "com.tbs.shared.mapper")
@EntityScan("com.tbs.shared.pojo.entity")
public class AppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

}
