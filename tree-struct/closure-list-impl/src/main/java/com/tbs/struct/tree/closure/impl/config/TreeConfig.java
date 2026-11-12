package com.tbs.struct.tree.closure.impl.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.tbs.struct.tree.closure.impl.mapper")
public class TreeConfig {
}
