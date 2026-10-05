package com.tbs.app.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * 推送消息中心实体与 Mapper 扫描配置
 * <p>
 * 消息中心实体与 Mapper 自包含于 default-system-impl（应用私有域，不入 shared）， 仿 dic 模块自包含先例，通过本配置将专属包纳入 tk.mybatis 扫描范围： mapper 扫描
 * com.tbs.app.mapper.push，实体扫描 com.tbs.app.entity.push。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
@MapperScan(basePackages = "com.tbs.shared.mapper.push")
@EntityScan(basePackages = "com.tbs.shared.pojo.entity.push")
public class PushMessageScanConfig {

}
