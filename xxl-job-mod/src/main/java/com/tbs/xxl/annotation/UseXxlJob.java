package com.tbs.xxl.annotation;

import com.tbs.common.annotation.EnableCommonPack;
import com.tbs.xxl.config.XxlAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 使用Xxl-job
 *
 * @author abstergo
 */
@Import(XxlAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@EnableCommonPack
@Target(ElementType.TYPE)
public @interface UseXxlJob {
}
