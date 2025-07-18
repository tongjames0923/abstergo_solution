package com.tbs.sql.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * The interface Query fields.
 * @author abstergo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Component
public @interface QueryFields {
    /**
     * Value query field [ ].
     *
     * @return the query field [ ]
     */
    QueryField[] value();

}
