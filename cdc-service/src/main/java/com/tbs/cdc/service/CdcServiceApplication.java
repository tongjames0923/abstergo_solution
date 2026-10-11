package com.tbs.cdc.service;

import com.tbs.cdc.annotation.EnableTbsCdc;
import com.tbs.cdc.sqlserver.annotation.EnableCdcSqlserver;
import com.tbs.common.annotation.EnableCommonPack;
import com.tbs.rabbitmq.annotation.EnableRabbitMq;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CDC MQ发出服务启动类
 * <p>
 * 独立可运行的数据库变更捕获服务：启用CDC运行时与SQL Server捕获源，
 * 捕获到的{@code CdcEvent}经进程内总线路由到{@code CdcMqPublisher}，
 * 按可配置的交换机与路由键模板发出到RabbitMQ，供下游消费者对接。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.tbs.cdc.service"})
@EnableCommonPack
@EnableTbsCdc
@EnableCdcSqlserver
@EnableRabbitMq
public class CdcServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdcServiceApplication.class, args);
    }

}
