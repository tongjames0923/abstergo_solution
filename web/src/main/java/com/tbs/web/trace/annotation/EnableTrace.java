package com.tbs.web.trace.annotation;

import com.tbs.web.trace.config.TraceConfig;
import com.tbs.web.trace.config.TraceProperty;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import({TraceConfig.class, TraceProperty.class})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface EnableTrace {
}
