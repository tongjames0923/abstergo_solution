package com.tbs.shared.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * 配置实体类扫描
 *
 * @author tongj
 */
@Configuration
@MapperScan(basePackages = "com.tbs.shared.mapper")
@EntityScan({"com.tbs.shared.pojo.entity", "com.tbs.shared.pojo.entity.view"})
public class SharedEntityConfig {
}
