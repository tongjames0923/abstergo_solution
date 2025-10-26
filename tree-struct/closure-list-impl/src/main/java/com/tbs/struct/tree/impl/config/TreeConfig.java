package com.tbs.struct.tree.impl.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan("com.tbs.struct.tree.impl.entity")
@MapperScan("com.tbs.struct.tree.impl.mapper")
public class TreeConfig {
}
