package com.tbs.app.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 推送消息中心 Mapper 扫描配置
 * <p>
 * 消息中心 Mapper 自包含于 default-system-impl（应用私有域，不入 shared）， 仿 dic 模块自包含先例，
 * 通过本配置将 com.tbs.shared.mapper.push 纳入 MyBatis Mapper 扫描范围；
 * 实体扫描由 jkit.sql.auto.packages 配置。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
@MapperScan(basePackages = "com.tbs.shared.mapper.push")
public class PushMessageScanConfig {

}
