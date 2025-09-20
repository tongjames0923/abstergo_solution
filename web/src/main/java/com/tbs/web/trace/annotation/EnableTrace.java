package com.tbs.web.trace.annotation;

import com.tbs.web.trace.config.TraceConfig;
import com.tbs.web.trace.config.TraceProperty;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Import({TraceConfig.class, TraceProperty.class})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableTrace {
}
