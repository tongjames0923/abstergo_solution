package com.tbs.app.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * 应用持久化配置
 * <p>
 * 注册应用私有实体与Mapper的扫描路径；
 * 逻辑删除拦截器SqlLogicDeleteIntercepter已由DefaultAppConfig注册，此处无需重复声明
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
@MapperScan(basePackages = "com.tbs.app.mapper")
@EntityScan(basePackages = "com.tbs.app.entity")
public class AppPersistenceConfig {
}

