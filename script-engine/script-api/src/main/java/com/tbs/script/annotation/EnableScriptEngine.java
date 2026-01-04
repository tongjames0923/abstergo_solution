package com.tbs.script.annotation;

import com.tbs.script.config.AutoScriptRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用脚本引擎自动注册
 *
 * @author tongj
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AutoScriptRegister.class)
public @interface EnableScriptEngine {
}
