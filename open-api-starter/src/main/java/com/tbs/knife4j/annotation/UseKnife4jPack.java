package com.tbs.knife4j.annotation;

import com.tbs.knife4j.config.Knife4jAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import(Knife4jAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface UseKnife4jPack {
}
