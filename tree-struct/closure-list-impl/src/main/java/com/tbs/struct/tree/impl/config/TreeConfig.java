package com.tbs.struct.tree.impl.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import tk.mybatis.spring.annotation.MapperScan;

@Configuration
@EntityScan("com.tbs.struct.tree.impl.entity")
@MapperScan("com.tbs.struct.tree.impl.mapper")
public class TreeConfig {
}
