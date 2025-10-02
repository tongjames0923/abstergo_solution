package com.tbs.app;

import com.tbs.sql.annotation.EnableTbsSqlUtils;
import com.tbs.sql.annotation.dialect.MYSQL;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
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
@MapperScan(basePackages = "com.tbs.shared.mapper")
@EntityScan({"com.tbs.shared.pojo.entity", "com.tbs.shared.pojo.entity.view"})
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableDiscoveryClient
//@EnableTrace
public class AppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

}