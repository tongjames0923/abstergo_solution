package com.tbs.struct.tree.closure.impl.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import tk.mybatis.spring.annotation.MapperScan;

@Configuration
@EntityScan("com.tbs.struct.tree.closure.impl.entity")
@MapperScan(basePackages = "com.tbs.struct.tree.closure.impl.mapper")
public class TreeConfig {
}
