package com.tbs.view.annotation;

import com.tbs.view.config.ViewAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用视图控制功能
 *
 * @author tongj
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ViewAutoRegister.class)
@Inherited
@Documented
public @interface EnableViewControl {
}
