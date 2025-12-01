package com.tbs.struct.tree.closure.annotation;

import com.tbs.common.annotation.EnableCommonPack;
import com.tbs.sql.annotation.EnableTbsSqlUtils;
import com.tbs.struct.tree.closure.impl.config.ClosureTableAutoRegister;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import tk.mybatis.spring.annotation.MapperScan;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 启用闭包表树形结构
 *
 * @author tongj
 */
@EnableCommonPack
@EnableTbsSqlUtils
@Import(ClosureTableAutoRegister.class)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.TYPE)
@Inherited
@Documented
public @interface UseClosureTableTree {
}
