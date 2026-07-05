package com.tbs.trace.annotation;

import com.tbs.trace.config.TraceAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import(TraceAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface EnableTrace {
}
